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

import static org.cxbox.source.dto.WorkflowTransitionConditionGroupDto_.name;
import static org.cxbox.source.dto.WorkflowTransitionConditionGroupDto_.seq;

import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.model.workflow.entity.WorkflowTransitionConditionGroup;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTransitionConditionGroupDtoConstructor extends
		DtoConstructor<WorkflowTransitionConditionGroup, WorkflowTransitionConditionGroupDto> {

	public WorkflowTransitionConditionGroupDtoConstructor() {
		super(WorkflowTransitionConditionGroup.class, WorkflowTransitionConditionGroupDto.class);
	}

	@Override
	protected Map<DtoField<? super WorkflowTransitionConditionGroupDto, ?>, ValueSupplier<? super WorkflowTransitionConditionGroup, ? super WorkflowTransitionConditionGroupDto, ?>> buildValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowTransitionConditionGroupDto, ?>, ValueSupplier<? super WorkflowTransitionConditionGroup, ? super WorkflowTransitionConditionGroupDto, ?>>builder()
				.put(seq, (mapping, entity) -> entity.getSeq())
				.put(name, (mapping, entity) -> entity.getName())
				.build();
	}

}
