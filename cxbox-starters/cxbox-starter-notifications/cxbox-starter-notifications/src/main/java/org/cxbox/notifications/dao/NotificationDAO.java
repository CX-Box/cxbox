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

package org.cxbox.notifications.dao;

import org.cxbox.api.data.PageSpecification;
import org.cxbox.api.data.ResultPage;
import org.cxbox.notifications.model.entity.Notification;
import org.cxbox.notifications.service.NotificationDeferredResult;

import java.util.List;
import java.util.Map;


public interface NotificationDAO {

	ResultPage<Notification> getNotifications(Long recipientId, boolean unread, Long offset, PageSpecification page);

	long countNotifications(Long recipientId, boolean unread, Long offset);

	Map<Long, List<Notification>> checkNewNotifications(List<NotificationDeferredResult> recipients);

	void markNotificationsAsRead(List<Long> notificationId, Boolean mark, Long recipientId);

	void deleteNotifications(List<Long> notificationId, Long recipientId);

	Long saveNotification(String url, String message, Long recipientId);

	void markDelivered(Notification notification, int serviceId);

}
