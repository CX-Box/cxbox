
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
import org.cxbox.model.workflow.entity.TaskField;
import org.cxbox.source.dto.WorkflowTaskFieldDto;
import org.cxbox.source.dto.WorkflowTaskFieldDto_;
import org.cxbox.source.services.data.WorkflowTaskFieldsService;
import org.cxbox.source.services.meta.WorkflowTaskFieldsFieldMetaBuilder;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTaskFieldsServiceImpl extends
		VersionAwareResponseService<WorkflowTaskFieldDto, TaskField> implements WorkflowTaskFieldsService {

	public WorkflowTaskFieldsServiceImpl() {
		super(WorkflowTaskFieldDto.class, TaskField.class, null, WorkflowTaskFieldsFieldMetaBuilder.class);
	}

	@Override
	protected CreateResult<WorkflowTaskFieldDto> doCreateEntity(final TaskField entity, final BusinessComponent bc) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ActionResultDTO<WorkflowTaskFieldDto> doUpdateEntity(TaskField entity, WorkflowTaskFieldDto dto,
			BusinessComponent bc) {
		if (dto.isFieldChanged(WorkflowTaskFieldDto_.title)) {
			entity.setTitle(dto.getTitle());
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	@Override
	public Actions<WorkflowTaskFieldDto> getActions() {
		return Actions.<WorkflowTaskFieldDto>builder()
				.save().add()
				.build();
	}

}
