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

package org.cxbox.source.services.data.impl;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.model.workflow.entity.TaskField;
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowStepField;
import org.cxbox.model.workflow.entity.WorkflowStepField_;
import org.cxbox.source.dto.WorkflowStepFieldDto;
import org.cxbox.source.dto.WorkflowStepFieldDto_;
import org.cxbox.source.services.data.WorkflowStepFieldService;
import org.cxbox.source.services.meta.WorkflowStepFieldFieldMetaBuilder;
import org.springframework.stereotype.Service;

@Service
public class WorkflowStepFieldServiceImpl extends
		VersionAwareResponseService<WorkflowStepFieldDto, WorkflowStepField> implements WorkflowStepFieldService {

	public WorkflowStepFieldServiceImpl() {
		super(
				WorkflowStepFieldDto.class,
				WorkflowStepField.class,
				WorkflowStepField_.step,
				WorkflowStepFieldFieldMetaBuilder.class
		);
	}

	@Override
	protected CreateResult<WorkflowStepFieldDto> doCreateEntity(final WorkflowStepField entity,
			final BusinessComponent bc) {
		entity.setStep(baseDAO.findById(WorkflowStep.class, bc.getParentIdAsLong()));
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	public ActionResultDTO<WorkflowStepFieldDto> doUpdateEntity(WorkflowStepField entity, WorkflowStepFieldDto dto,
			BusinessComponent bc) {
		if (dto.isFieldChanged(WorkflowStepFieldDto_.fieldId)) {
			entity.setTaskField(dto.getFieldId() == null ? null : baseDAO.findById(TaskField.class, dto.getFieldId()));
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	@Override
	public Actions<WorkflowStepFieldDto> getActions() {
		return Actions.<WorkflowStepFieldDto>builder()
				.create().add()
				.save().add()
				.delete().add()
				.build();
	}

}
