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

package org.cxbox.notifications.crudma.meta;



import org.cxbox.api.data.dictionary.DictionaryType;

import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.notifications.crudma.config.NotificationServiceAssociation;
import org.cxbox.notifications.crudma.dto.NotificationRecipientDTO;
import org.cxbox.notifications.crudma.dto.NotificationRecipientDTO_;
import org.springframework.stereotype.Service;


@Service
public class NotificationRecipientFieldMetaBuilder extends FieldMetaBuilder<NotificationRecipientDTO> {

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<NotificationRecipientDTO> fields,
			BcDescription bcDescription, Long id, Long parentId) {
		if (NotificationServiceAssociation.notificationRecipients.isBc(bcDescription)) {
			fields.setEnabled(NotificationRecipientDTO_.recipientType);
			fields.setEnabled(NotificationRecipientDTO_.sameDeptOnly);
			fields.setRequired(NotificationRecipientDTO_.recipientType);
		} else {
			fields.setEnabled(NotificationRecipientDTO_.enabled);
		}

		// глобальные настройки
		if (NotificationServiceAssociation.notificationRecipients.isBc(bcDescription)) {
			fields.setDictionaryTypeWithAllValues(NotificationRecipientDTO_.recipientType, DictionaryType.NOTIFICATION_RECIPIENT_TYPE);
		}
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<NotificationRecipientDTO> fields, BcDescription bcDescription,
			Long parentId) {

	}

}
