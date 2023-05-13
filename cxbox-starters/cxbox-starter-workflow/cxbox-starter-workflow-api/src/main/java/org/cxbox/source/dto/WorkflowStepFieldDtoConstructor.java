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

import static org.cxbox.source.dto.WorkflowStepFieldDto_.field;
import static org.cxbox.source.dto.WorkflowStepFieldDto_.fieldId;

import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.model.workflow.entity.TaskField;
import org.cxbox.model.workflow.entity.WorkflowStepField;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class WorkflowStepFieldDtoConstructor extends DtoConstructor<WorkflowStepField, WorkflowStepFieldDto> {

	public WorkflowStepFieldDtoConstructor() {
		super(WorkflowStepField.class, WorkflowStepFieldDto.class);
	}

	@Override
	protected Map<DtoField<? super WorkflowStepFieldDto, ?>, ValueSupplier<? super WorkflowStepField, ? super WorkflowStepFieldDto, ?>> buildValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowStepFieldDto, ?>, ValueSupplier<? super WorkflowStepField, ? super WorkflowStepFieldDto, ?>>builder()
				.put(fieldId, (mapping, entity) -> Optional.ofNullable(entity.getTaskField())
						.map(TaskField::getId)
						.orElse(null)
				)
				.put(field, (mapping, entity) -> Optional.ofNullable(entity.getTaskField())
						.map(TaskField::getTitle)
						.orElse(null)
				)
				.build();
	}

}
