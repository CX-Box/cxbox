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

package org.cxbox.source.dto;

import static org.cxbox.source.dto.WorkflowTaskMigrationDto_.currentAutomaticTransitionId;
import static org.cxbox.source.dto.WorkflowTaskMigrationDto_.currentAutomaticTransitionName;
import static org.cxbox.source.dto.WorkflowTaskMigrationDto_.currentStepId;
import static org.cxbox.source.dto.WorkflowTaskMigrationDto_.currentStepName;
import static java.util.Optional.ofNullable;

import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.core.util.SpringBeanUtils;
import org.cxbox.engine.workflow.services.WorkflowDao;
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowTask;
import org.cxbox.model.workflow.entity.WorkflowTransition;
import org.cxbox.model.workflow.entity.WorkflowableTask;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTaskMigrationDtoConstructor extends DtoConstructor<WorkflowableTask, WorkflowTaskMigrationDto> {

	public WorkflowTaskMigrationDtoConstructor() {
		super(WorkflowableTask.class, WorkflowTaskMigrationDto.class);
	}

	@Override
	public Map<DtoField<? super WorkflowTaskMigrationDto, ?>, ValueSupplier<? super WorkflowableTask, ? super WorkflowTaskMigrationDto, ?>> buildValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowTaskMigrationDto, ?>, ValueSupplier<? super WorkflowableTask, ? super WorkflowTaskMigrationDto, ?>>builder()
				.put(currentStepId, (mapping, entity) -> ofNullable(entity.getWorkflowTask())
						.map(WorkflowTask::getWorkflowStep)
						.map(WorkflowStep::getId)
						.orElse(null)
				)
				.put(currentStepName, (mapping, entity) -> ofNullable(entity.getWorkflowTask())
						.map(WorkflowTask::getWorkflowStep)
						.map(WorkflowStep::getName)
						.orElse(null)
				)
				.put(currentAutomaticTransitionId, (mapping, entity) -> entity.getAutomaticTransitionName())
				.put(currentAutomaticTransitionName, (mapping, entity) -> ofNullable(entity.getAutomaticTransitionName())
						.map(uuid -> SpringBeanUtils.getBean(WorkflowDao.class).getActiveWorkflowTransitionByName(uuid))
						.map(WorkflowTransition::getName)
						.orElse(null)
				)
				.build();
	}

}
