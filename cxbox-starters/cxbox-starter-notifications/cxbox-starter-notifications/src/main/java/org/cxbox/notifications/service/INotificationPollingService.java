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

package org.cxbox.notifications.service;

import static org.cxbox.core.util.DateTimeUtil.switchZone;

import org.cxbox.core.dto.ResponseDTO;
import org.cxbox.notifications.dto.NotificationDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZoneId;

import org.cxbox.notifications.model.entity.Notification;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.context.request.async.DeferredResult;


public interface INotificationPollingService {

	static NotificationDTO entityToDto(Notification entity) {
		return new NotificationCtrlDTO(entity);
	}

	static NotificationDTO entityToDto(Notification entity, ZoneId zoneId) {
		return setClientTime(entityToDto(entity), zoneId);
	}

	static NotificationDTO setClientTime(NotificationDTO dto, ZoneId zoneId) {
		dto.setCreatedDate(switchZone(dto.getCreatedDate(), ZoneId.systemDefault(), zoneId));
		return dto;
	}

	DeferredResult<ResponseDTO> addTaskInQueue(Long recipientId, Long latestNotificationId, boolean unread);

	class NotificationCtrlDTO extends NotificationDTO {

		private NotificationCtrlDTO(Notification entity) {
			super(entity);
		}

		@Override
		@JsonIgnore
		public String getId() {
			return super.getId();
		}

		@JsonProperty("id")
		public Long getIdAsLong() {
			return NumberUtils.createLong(getId());
		}

	}

}
