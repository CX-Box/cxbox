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

package org.cxbox.source.services.data.impl;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowTaskChildBcAvailability;
import org.cxbox.model.workflow.entity.WorkflowTaskChildBcAvailability_;
import org.cxbox.source.dto.WorkflowTaskChildBcAvailabilityDto;
import org.cxbox.source.dto.WorkflowTaskChildBcAvailabilityDto_;
import org.cxbox.source.services.data.WorkflowTaskChildBcAvailabilityService;
import org.cxbox.source.services.meta.WorkflowTaskChildBcAvailabilityFieldMetaBuilder;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTaskChildBcAvailabilityServiceImpl extends
		VersionAwareResponseService<WorkflowTaskChildBcAvailabilityDto, WorkflowTaskChildBcAvailability> implements
		WorkflowTaskChildBcAvailabilityService {

	public WorkflowTaskChildBcAvailabilityServiceImpl() {
		super(
				WorkflowTaskChildBcAvailabilityDto.class,
				WorkflowTaskChildBcAvailability.class,
				WorkflowTaskChildBcAvailability_.workflowStep,
				WorkflowTaskChildBcAvailabilityFieldMetaBuilder.class
		);
	}

	@Override
	protected CreateResult<WorkflowTaskChildBcAvailabilityDto> doCreateEntity(
			final WorkflowTaskChildBcAvailability entity, final BusinessComponent bc) {
		entity.setWorkflowStep(baseDAO.findById(WorkflowStep.class, bc.getParentIdAsLong()));
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	protected ActionResultDTO<WorkflowTaskChildBcAvailabilityDto> doUpdateEntity(WorkflowTaskChildBcAvailability entity,
			WorkflowTaskChildBcAvailabilityDto dto, BusinessComponent bc) {
		if (dto.isFieldChanged(WorkflowTaskChildBcAvailabilityDto_.bcName)) {
			entity.setBcName(dto.getBcName());
		}
		if (dto.isFieldChanged(WorkflowTaskChildBcAvailabilityDto_.affectedWidgets)) {
			entity.setAffectedWidgets(dto.getAffectedWidgets());
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	@Override
	public Actions<WorkflowTaskChildBcAvailabilityDto> getActions() {
		return Actions.<WorkflowTaskChildBcAvailabilityDto>builder()
				.create().add()
				.save().add()
				.delete().add()
				.build();
	}

}
