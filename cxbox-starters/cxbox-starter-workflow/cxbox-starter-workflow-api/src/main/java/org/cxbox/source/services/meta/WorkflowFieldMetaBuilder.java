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

import static org.cxbox.source.dto.WorkflowDto_.activeVersion;
import static org.cxbox.source.dto.WorkflowDto_.deptShortName;
import static org.cxbox.source.dto.WorkflowDto_.description;
import static org.cxbox.source.dto.WorkflowDto_.name;
import static org.cxbox.source.dto.WorkflowDto_.taskTypeCd;

import org.cxbox.WorkflowServiceAssociation;
import org.cxbox.api.data.dictionary.DictionaryType;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.source.dto.WorkflowDto;
import org.springframework.stereotype.Service;

@Service
public class WorkflowFieldMetaBuilder extends FieldMetaBuilder<WorkflowDto> {

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<WorkflowDto> fields, InnerBcDescription bcDescription,
			Long rowId, Long parRowId) {
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<WorkflowDto> fields, InnerBcDescription bcDescription, Long parRowId) {
		if (WorkflowServiceAssociation.migrationWf.isNotBc(bcDescription)) {
			fields.setEnabled(name, description, taskTypeCd, activeVersion, deptShortName);
			fields.enableFilter(name, description, taskTypeCd, deptShortName);
			fields.setDictionaryTypeWithAllValues(taskTypeCd, DictionaryType.TASK_TYPE);
			fields.setAllFilterValuesByLovType(taskTypeCd, DictionaryType.TASK_TYPE);
		}
	}

}
