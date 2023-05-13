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

package org.cxbox.core.exception;

import org.cxbox.core.dto.BusinessError.Entity;
import org.cxbox.model.core.entity.BaseEntity;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

@Slf4j
public class InnerBcException extends BusinessException {

	public InnerBcException(final BaseEntity entity, final String logMessage, final String uiMessage) {
		log.error(logMessage);
		Entity errorEntity;
		if (entity != null) {
			errorEntity = new Entity("InnerBusinessComponent", entity.getId().toString());
			for (Field field : Hibernate.getClass(entity).getFields()) {
				errorEntity.addField(field.getName(), field.getType().getSimpleName());
			}
		} else {
			errorEntity = new Entity("InnerBusinessComponent", "unknown");
		}
		super.addPopup(uiMessage).setEntity(errorEntity);
	}

}
