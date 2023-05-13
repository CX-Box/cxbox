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

import static org.cxbox.source.dto.WorkflowStepConditionGroupDto_.name;
import static org.cxbox.source.dto.WorkflowStepConditionGroupDto_.seq;

import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.model.workflow.entity.WorkflowStepConditionGroup;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorkflowStepConditionGroupDtoConstructor extends
		DtoConstructor<WorkflowStepConditionGroup, WorkflowStepConditionGroupDto> {

	public WorkflowStepConditionGroupDtoConstructor() {
		super(WorkflowStepConditionGroup.class, WorkflowStepConditionGroupDto.class);
	}

	@Override
	protected Map<DtoField<? super WorkflowStepConditionGroupDto, ?>, ValueSupplier<? super WorkflowStepConditionGroup, ? super WorkflowStepConditionGroupDto, ?>> buildValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowStepConditionGroupDto, ?>, ValueSupplier<? super WorkflowStepConditionGroup, ? super WorkflowStepConditionGroupDto, ?>>builder()
				.put(seq, (mapping, entity) -> entity.getSeq())
				.put(name, (mapping, entity) -> entity.getName())
				.build();
	}

}
