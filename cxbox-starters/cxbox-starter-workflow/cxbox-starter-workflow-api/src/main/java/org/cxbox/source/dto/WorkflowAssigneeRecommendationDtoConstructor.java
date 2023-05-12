
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

import static org.cxbox.source.dto.WorkflowAssigneeRecommendationDto_.condAssigneeCd;
import static org.cxbox.source.dto.WorkflowAssigneeRecommendationDto_.department;
import static org.cxbox.source.dto.WorkflowAssigneeRecommendationDto_.departmentId;
import static org.cxbox.source.dto.WorkflowAssigneeRecommendationDto_.description;
import static java.util.Optional.ofNullable;

import org.cxbox.constgen.DtoField;
import org.cxbox.core.dict.WorkflowDictionaryType;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.model.core.entity.Department;
import org.cxbox.model.workflow.entity.WorkflowAssigneeRecommendation;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorkflowAssigneeRecommendationDtoConstructor extends
		DtoConstructor<WorkflowAssigneeRecommendation, WorkflowAssigneeRecommendationDto> {

	public WorkflowAssigneeRecommendationDtoConstructor() {
		super(WorkflowAssigneeRecommendation.class, WorkflowAssigneeRecommendationDto.class);
	}

	@Override
	protected Map<DtoField<? super WorkflowAssigneeRecommendationDto, ?>, ValueSupplier<? super WorkflowAssigneeRecommendation, ? super WorkflowAssigneeRecommendationDto, ?>> buildValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowAssigneeRecommendationDto, ?>, ValueSupplier<? super WorkflowAssigneeRecommendation, ? super WorkflowAssigneeRecommendationDto, ?>>builder()
				.put(condAssigneeCd, (mapping, entity) -> WorkflowDictionaryType.WF_COND_ASSIGNEE.lookupValue(
						entity.getCondAssigneeCd()
				))
				.put(departmentId, (mapping, entity) -> ofNullable(entity.getDepartment())
						.map(Department::getId)
						.orElse(null)
				)
				.put(department, (mapping, entity) -> ofNullable(entity.getDepartment())
						.map(Department::getShortName)
						.orElse(null)
				)
				.put(description, (mapping, entity) -> entity.getDescription())
				.build();
	}

}
