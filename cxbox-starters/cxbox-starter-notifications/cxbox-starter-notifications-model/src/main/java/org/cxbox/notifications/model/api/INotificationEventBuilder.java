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

package org.cxbox.notifications.model.api;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.model.core.entity.User;
import java.util.Collection;
import java.util.Map;


public interface INotificationEventBuilder {

	INotificationEventBuilder addDefaultRecipients(boolean value);

	INotificationEventBuilder setMimeType(LOV mimeType);

	INotificationEventBuilder addRecipients(LOV recipientRole, Collection<User> users);

	INotificationEventBuilder addRecipient(LOV recipientRole, User user);

	INotificationEventBuilder addRecipient(LOV recipientRole);

	INotificationEventBuilder setPerformer(User user);

	INotificationEventBuilder excludeUser(User user);

	INotificationEventBuilder excludePerformer();

	INotificationEventBuilder addModel(String name, Object value);

	INotificationEventBuilder addModel(Map<String, Object> model);

	void publish();

	void publish(boolean async);

}
