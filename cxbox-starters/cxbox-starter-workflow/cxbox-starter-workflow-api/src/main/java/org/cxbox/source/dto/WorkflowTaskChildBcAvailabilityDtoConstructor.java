
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

import static org.cxbox.source.dto.WorkflowTaskChildBcAvailabilityDto_.affectedWidgets;
import static org.cxbox.source.dto.WorkflowTaskChildBcAvailabilityDto_.bcName;

import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.model.workflow.entity.WorkflowTaskChildBcAvailability;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTaskChildBcAvailabilityDtoConstructor extends
		DtoConstructor<WorkflowTaskChildBcAvailability, WorkflowTaskChildBcAvailabilityDto> {

	public WorkflowTaskChildBcAvailabilityDtoConstructor() {
		super(WorkflowTaskChildBcAvailability.class, WorkflowTaskChildBcAvailabilityDto.class);
	}

	@Override
	protected Map<DtoField<? super WorkflowTaskChildBcAvailabilityDto, ?>, ValueSupplier<? super WorkflowTaskChildBcAvailability, ? super WorkflowTaskChildBcAvailabilityDto, ?>> buildValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowTaskChildBcAvailabilityDto, ?>, ValueSupplier<? super WorkflowTaskChildBcAvailability, ? super WorkflowTaskChildBcAvailabilityDto, ?>>builder()
				.put(bcName, (mapping, entity) -> entity.getBcName())
				.put(affectedWidgets, (mapping, entity) -> entity.getAffectedWidgets())
				.build();
	}

}
