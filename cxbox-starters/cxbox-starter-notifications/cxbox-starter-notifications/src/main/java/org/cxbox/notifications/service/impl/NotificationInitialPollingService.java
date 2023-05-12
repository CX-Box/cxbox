
/*
 * © OOO "SI IKS LAB", 2022-2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cxbox.notifications.service.impl;

import org.cxbox.api.system.SystemSettings;
import org.cxbox.notifications.dao.NotificationDAO;
import org.cxbox.core.dto.ResponseDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

@Component
public class NotificationInitialPollingService extends AbstractNotificationPollingService {

	public NotificationInitialPollingService(SystemSettings systemSettings,
			NotificationDAO notificationDAO) {
		super(systemSettings, notificationDAO);
	}

	@Override
	public DeferredResult<ResponseDTO> addTaskInQueue(Long recipientId, Long latestNotificationId, boolean unread) {
		if (latestNotificationId >= 0) {
			return null;
		}
		return super.addTaskInQueue(recipientId, latestNotificationId, unread);
	}

}
