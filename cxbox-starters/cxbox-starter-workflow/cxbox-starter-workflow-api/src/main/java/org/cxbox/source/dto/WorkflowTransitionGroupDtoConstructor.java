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

package org.cxbox.source.dto;

import static org.cxbox.source.dto.WorkflowTransitionGroupDto_.description;
import static org.cxbox.source.dto.WorkflowTransitionGroupDto_.maxShowButtonsInGroup;
import static org.cxbox.source.dto.WorkflowTransitionGroupDto_.nameButtonYet;
import static org.cxbox.source.dto.WorkflowTransitionGroupDto_.workflowStepId;

import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.model.workflow.entity.WorkflowTransitionGroup;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTransitionGroupDtoConstructor extends
		DtoConstructor<WorkflowTransitionGroup, WorkflowTransitionGroupDto> {

	public WorkflowTransitionGroupDtoConstructor() {
		super(WorkflowTransitionGroup.class, WorkflowTransitionGroupDto.class);
	}

	@Override
	protected Map<DtoField<? super WorkflowTransitionGroupDto, ?>, ValueSupplier<? super WorkflowTransitionGroup, ? super WorkflowTransitionGroupDto, ?>> buildValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowTransitionGroupDto, ?>, ValueSupplier<? super WorkflowTransitionGroup, ? super WorkflowTransitionGroupDto, ?>>builder()
				.put(workflowStepId, (mapping, entity) -> entity.getWorkflowStep().getId())
				.put(maxShowButtonsInGroup, (mapping, entity) -> entity.getMaxShowButtonsInGroup())
				.put(nameButtonYet, (mapping, entity) -> entity.getNameButtonYet())
				.put(description, (mapping, entity) -> entity.getDescription())
				.build();
	}

}
