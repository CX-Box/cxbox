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

import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.notifications.dao.impl.NotificationSpecifications;
import org.cxbox.notifications.model.entity.Notification;
import org.cxbox.notifications.model.entity.Notification_;
import org.cxbox.notifications.service.IDeliveryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public abstract class AbstractDeliveryService implements IDeliveryService {

	@Autowired
	protected JpaDao jpaDao;


	public List<Notification> queryNotifications() {
		return jpaDao.getList(Notification.class, NotificationSpecifications.notDelivered(getServiceId()));
	}

	public final void markDelivered(Notification notification) {
		jpaDao.update(Notification.class, (root, cq, cb) -> cb.equal(root.get(Notification_.id), notification.getId()),
				NotificationSpecifications.markDelivered(getServiceId(), true)
		);
	}

}
