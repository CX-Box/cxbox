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

import static org.cxbox.source.dto.WorkflowTransitionDto_.backgroundExecution;
import static org.cxbox.source.dto.WorkflowTransitionDto_.checkRequiredFields;
import static org.cxbox.source.dto.WorkflowTransitionDto_.name;
import static org.cxbox.source.dto.WorkflowTransitionDto_.workflowDestStepId;
import static org.cxbox.source.dto.WorkflowTransitionDto_.workflowDestStepName;
import static org.cxbox.source.dto.WorkflowTransitionDto_.workflowGroupDescription;
import static org.cxbox.source.dto.WorkflowTransitionDto_.workflowGroupNameButtonYet;
import static org.cxbox.source.dto.WorkflowTransitionDto_.workflowName;
import static org.cxbox.source.dto.WorkflowTransitionDto_.workflowTransitionGroupId;
import static org.cxbox.source.dto.WorkflowTransitionDto_.workflowVersion;
import static java.util.Optional.ofNullable;

import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowTransition;
import org.cxbox.model.workflow.entity.WorkflowTransitionGroup;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTransitionDtoConstructor extends DtoConstructor<WorkflowTransition, WorkflowTransitionDto> {

	public WorkflowTransitionDtoConstructor() {
		super(WorkflowTransition.class, WorkflowTransitionDto.class);
	}

	@Override
	protected Map<DtoField<? super WorkflowTransitionDto, ?>, ValueSupplier<? super WorkflowTransition, ? super WorkflowTransitionDto, ?>> buildValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowTransitionDto, ?>, ValueSupplier<? super WorkflowTransition, ? super WorkflowTransitionDto, ?>>builder()
				.put(name, (mapping, entity) -> entity.getName())
				.put(workflowVersion, (mapping, entity) -> entity.getSourceStep().getWorkflowVersion().getVersion())
				.put(workflowName, (mapping, entity) -> entity.getSourceStep().getWorkflowVersion().getWorkflow().getName())
				.put(workflowDestStepId, (mapping, entity) -> ofNullable(entity.getDestinationStep())
						.map(WorkflowStep::getId)
						.orElse(null)
				)
				.put(workflowDestStepName, (mapping, entity) -> ofNullable(entity.getDestinationStep())
						.map(WorkflowStep::getName)
						.orElse(null)
				)
				.put(workflowTransitionGroupId, (mapping, entity) -> ofNullable(entity.getWorkflowTransitionGroup())
						.map(BaseEntity::getId)
						.orElse(null)
				)
				.put(workflowGroupDescription, (mapping, entity) -> ofNullable(entity.getWorkflowTransitionGroup())
						.map(WorkflowTransitionGroup::getDescription)
						.orElse(null)
				)
				.put(workflowGroupNameButtonYet, (mapping, entity) -> ofNullable(entity.getWorkflowTransitionGroup())
						.map(WorkflowTransitionGroup::getNameButtonYet)
						.orElse(null)
				)
				.put(checkRequiredFields, (mapping, entity) -> entity.getCheckRequiredFields())
				.put(backgroundExecution, (mapping, entity) -> entity.getBackgroundExecution())
				.build();
	}

}
