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

package org.cxbox.source.services.meta;

import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.engine.workflow.dao.WorkflowableTaskDao;
import org.cxbox.source.dto.WorkflowTaskMigrationDto;
import org.cxbox.source.dto.WorkflowTaskMigrationDto_;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowTaskMigrationFieldMetaBuilder extends FieldMetaBuilder<WorkflowTaskMigrationDto> {

	private final WorkflowableTaskDao<?> workflowableTaskDao;

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<WorkflowTaskMigrationDto> fields,
			InnerBcDescription bcDescription, Long rowId, Long parRowId) {
		if (workflowableTaskDao.getTask(rowId).getAutomaticTransitionName() != null) {
			fields.setRequired(WorkflowTaskMigrationDto_.newAutomaticTransitionName);
		}
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<WorkflowTaskMigrationDto> fields, InnerBcDescription bcDescription,
			Long parRowId) {
		fields.setEnabled(WorkflowTaskMigrationDto_.newStepName, WorkflowTaskMigrationDto_.newAutomaticTransitionName);
		fields.setRequired(WorkflowTaskMigrationDto_.newStepName);
		fields.enableFilter(
				WorkflowTaskMigrationDto_.currentStepName,
				WorkflowTaskMigrationDto_.currentAutomaticTransitionName
		);
	}

}
