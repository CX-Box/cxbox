/*-
 * #%L
 * IO Cxbox - Workflow API
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

package org.cxbox.source.services.meta;

import static org.cxbox.source.dto.WorkflowTaskFieldDto_.key;
import static org.cxbox.source.dto.WorkflowTaskFieldDto_.title;

import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.source.dto.WorkflowTaskFieldDto;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTaskFieldsFieldMetaBuilder extends FieldMetaBuilder<WorkflowTaskFieldDto> {

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<WorkflowTaskFieldDto> fields,
			InnerBcDescription bcDescription, Long rowId, Long parRowId) {
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<WorkflowTaskFieldDto> fields, InnerBcDescription bcDescription,
			Long parRowId) {
		fields.setEnabled(title);
		fields.enableFilter(key, title);
	}

}
