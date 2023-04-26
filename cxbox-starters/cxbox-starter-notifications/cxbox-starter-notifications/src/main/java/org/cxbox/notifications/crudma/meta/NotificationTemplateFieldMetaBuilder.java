/*-
 * #%L
 * IO Cxbox - Source
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

package org.cxbox.notifications.crudma.meta;

import org.cxbox.api.data.dictionary.DictionaryType;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.notifications.crudma.dto.NotificationTemplateDTO;
import org.springframework.stereotype.Service;

import static org.cxbox.notifications.crudma.dto.NotificationTemplateDTO_.*;

@Service
public class NotificationTemplateFieldMetaBuilder extends FieldMetaBuilder<NotificationTemplateDTO> {

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<NotificationTemplateDTO> fields,
			InnerBcDescription bcDescription, Long rowId, Long parRowId) {
		fields.setEnabled(
				eventName,
				mimeType,
				subject,
				message,
				uiSubject,
				uiMessage,
				url,
				taskId,
				taskName,
				cntrpId,
				cntrpName
		);
		fields.setRequired(eventName, mimeType, subject, message, uiSubject, uiMessage);
		fields.setDictionaryTypeWithAllValues(eventName, DictionaryType.DATABASE_EVENT);
		fields.setDictionaryTypeWithAllValues(mimeType, DictionaryType.MIME_TYPE);
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<NotificationTemplateDTO> fields, InnerBcDescription bcDescription,
			Long parRowId) {
		fields.enableFilter(eventName, mimeType);
		fields.setEphemeral(taskId, taskName, cntrpId, cntrpName);
		fields.setAllFilterValuesByLovType(eventName, DictionaryType.DATABASE_EVENT);
		fields.setAllFilterValuesByLovType(mimeType, DictionaryType.MIME_TYPE);
	}

}
