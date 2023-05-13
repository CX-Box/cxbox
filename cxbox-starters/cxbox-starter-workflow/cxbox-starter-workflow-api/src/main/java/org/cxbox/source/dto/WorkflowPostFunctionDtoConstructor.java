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

import static org.cxbox.source.dto.WorkflowPostFunctionDto_.actionCd;
import static org.cxbox.source.dto.WorkflowPostFunctionDto_.seq;
import static org.cxbox.source.dto.WorkflowPostFunctionDto_.stepTerm;

import org.cxbox.constgen.DtoField;
import org.cxbox.core.dict.WorkflowDictionaryType;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.model.workflow.entity.WorkflowPostFunction;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorkflowPostFunctionDtoConstructor extends DtoConstructor<WorkflowPostFunction, WorkflowPostFunctionDto> {

	public WorkflowPostFunctionDtoConstructor() {
		super(WorkflowPostFunction.class, WorkflowPostFunctionDto.class);
	}

	@Override
	protected Map<DtoField<? super WorkflowPostFunctionDto, ?>, ValueSupplier<? super WorkflowPostFunction, ? super WorkflowPostFunctionDto, ?>> buildValueSuppliers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<DtoField<? super WorkflowPostFunctionDto, ?>, ValueSupplier<? super WorkflowPostFunction, ? super WorkflowPostFunctionDto, ?>> getValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowPostFunctionDto, ?>, ValueSupplier<? super WorkflowPostFunction, ? super WorkflowPostFunctionDto, ?>>builder()
				.put(seq, (mapping, entity) -> entity.getSeq())
				.put(actionCd, (mapping, entity) -> WorkflowDictionaryType.WF_TRN_ACT.lookupValue(entity.getActionCd()))
				.put(stepTerm, (mapping, entity) -> entity.getStepTerm())
				.build();
	}

}
