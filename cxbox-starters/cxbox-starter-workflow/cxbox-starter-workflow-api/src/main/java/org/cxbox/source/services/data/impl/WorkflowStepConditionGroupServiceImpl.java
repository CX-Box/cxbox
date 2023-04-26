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
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowStepConditionGroup;
import org.cxbox.model.workflow.entity.WorkflowStepConditionGroup_;
import org.cxbox.source.dto.WorkflowStepConditionGroupDto;
import org.cxbox.source.dto.WorkflowStepConditionGroupDto_;
import org.cxbox.source.services.data.WorkflowStepConditionGroupService;
import org.cxbox.source.services.meta.WorkflowStepConditionGroupFieldMetaBuilder;
import org.springframework.stereotype.Service;

@Service
public class WorkflowStepConditionGroupServiceImpl extends
		VersionAwareResponseService<WorkflowStepConditionGroupDto, WorkflowStepConditionGroup> implements
		WorkflowStepConditionGroupService {

	public WorkflowStepConditionGroupServiceImpl() {
		super(
				WorkflowStepConditionGroupDto.class,
				WorkflowStepConditionGroup.class,
				WorkflowStepConditionGroup_.step,
				WorkflowStepConditionGroupFieldMetaBuilder.class
		);
	}

	@Override
	protected CreateResult<WorkflowStepConditionGroupDto> doCreateEntity(final WorkflowStepConditionGroup entity,
			final BusinessComponent bc) {
		entity.setStep(baseDAO.findById(WorkflowStep.class, bc.getParentIdAsLong()));
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	protected ActionResultDTO<WorkflowStepConditionGroupDto> doUpdateEntity(WorkflowStepConditionGroup entity,
			WorkflowStepConditionGroupDto dto, BusinessComponent bc) {
		if (dto.isFieldChanged(WorkflowStepConditionGroupDto_.seq)) {
			entity.setSeq(dto.getSeq());
		}
		if (dto.isFieldChanged(WorkflowStepConditionGroupDto_.name)) {
			entity.setName(dto.getName());
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	@Override
	public Actions<WorkflowStepConditionGroupDto> getActions() {
		return Actions.<WorkflowStepConditionGroupDto>builder()
				.create().add()
				.save().add()
				.delete().add()
				.build();
	}

}
