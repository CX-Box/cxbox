/*-
 * #%L
 * IO Cxbox - Core
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.notifications.service.impl;

import org.cxbox.api.data.ResultPage;
import org.cxbox.api.system.ISystemSettingChangeEventListener;
import org.cxbox.api.system.SystemSettingChangedEvent;
import org.cxbox.api.system.SystemSettings;
import org.cxbox.api.util.tz.TimeZoneUtil;
import org.cxbox.core.dto.ResponseDTO;
import org.cxbox.core.util.ResponseBuilder;
import org.cxbox.notifications.dao.NotificationDAO;
import org.cxbox.notifications.model.entity.Notification;
import org.cxbox.notifications.service.INotificationPollingService;
import org.cxbox.notifications.service.NotificationDeferredResult;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;

import static org.cxbox.notifications.dictionary.NotificationDictionaries.SystemPref.FEATURE_NOTIFICATIONS;
import static org.cxbox.notifications.service.INotificationPollingService.entityToDto;
import static java.util.Comparator.comparingLong;
import static java.util.function.BinaryOperator.minBy;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
public class AbstractNotificationPollingService implements ISystemSettingChangeEventListener,
		INotificationPollingService {

	private final SystemSettings systemSettings;

	private final NotificationDAO notificationDAO;

	private final LinkedBlockingDeque<NotificationDeferredResult> responseBodyQueue = new LinkedBlockingDeque<>();

	private boolean enabled;

	@PostConstruct
	protected void init() {
		this.enabled = systemSettings.getBooleanValue(FEATURE_NOTIFICATIONS);
	}

	@Override
	public void onApplicationEvent(SystemSettingChangedEvent event) {
		if (FEATURE_NOTIFICATIONS.equals(event.getSetting())) {
			this.enabled = systemSettings.getBooleanValue(FEATURE_NOTIFICATIONS);
		}
	}

	@Override
	public DeferredResult<ResponseDTO> addTaskInQueue(Long recipientId, Long latestNotificationId,
			boolean unread) {
		NotificationDeferredResult result = new NotificationDeferredResult(
				recipientId,
				latestNotificationId,
				TimeZoneUtil.getSessionZoneId(),
				unread
		);
		if (enabled) {
			responseBodyQueue.add(result);
		}
		return result;
	}

	private void addToHead(NotificationDeferredResult result) {
		if (!result.isSetOrExpired()) {
			responseBodyQueue.addFirst(result);
		}
	}

	private Deque<NotificationDeferredResult> snapshot() {
		LinkedList<NotificationDeferredResult> result = new LinkedList<>();
		responseBodyQueue.drainTo(result);
		return result;
	}

	@Scheduled(fixedRate = 1000)
	public void executePollTaskInQueue() {
		if (!enabled) {
			return;
		}

		doExecutePollTaskInQueue();

	}

	private void doExecutePollTaskInQueue() {
		// текущий срез
		Deque<NotificationDeferredResult> snapshot = snapshot();
		// удаляем сразу все что протухло
		snapshot.removeIf(DeferredResult::isSetOrExpired);
		// нечего обрабатывать
		if (snapshot.isEmpty()) {
			return;
		}

		// запросы с минимальными latestNotificationId
		List<NotificationDeferredResult> recipients = new ArrayList<>(snapshot.stream().collect(
				toMap(
						NotificationDeferredResult::getRecipientId,
						Function.identity(),
						minBy(
								comparingLong(NotificationDeferredResult::getLatestNotificationId)
						)
				)
		).values());

		if (recipients.isEmpty()) {
			return;
		}

		Map<Long, List<Notification>> notifications =
				notificationDAO.checkNewNotifications(recipients);

		NotificationDeferredResult result;
		while ((result = snapshot.pollLast()) != null) {
			Long lastId = result.getLatestNotificationId();
			ZoneId zoneId = result.getZoneId();
			boolean unread = result.isUnread();
			boolean processed = notifications.getOrDefault(
					result.getRecipientId(),
					Collections.emptyList()
			).stream().filter(n -> n.getId() > lastId && !(unread && n.isRead()))
					.map(entity -> entityToDto(entity, zoneId))
					.collect(
							collectingAndThen(
									collectingAndThen(
											toList(), list -> ResponseBuilder.build(ResultPage.of(list, false))
									),
									result::setResult
							)
					);
		}
	}


}
