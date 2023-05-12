
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

import static org.cxbox.source.dto.WorkflowTransitionValidationDto_.dmn;
import static org.cxbox.source.dto.WorkflowTransitionValidationDto_.errorMessage;
import static org.cxbox.source.dto.WorkflowTransitionValidationDto_.preInvokeCond;
import static org.cxbox.source.dto.WorkflowTransitionValidationDto_.preInvokeMessage;
import static org.cxbox.source.dto.WorkflowTransitionValidationDto_.preInvokeType;
import static org.cxbox.source.dto.WorkflowTransitionValidationDto_.seq;
import static org.cxbox.source.dto.WorkflowTransitionValidationDto_.validCd;
import static org.cxbox.source.dto.WorkflowTransitionValidationDto_.validCdKey;
import static java.util.Optional.ofNullable;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.dict.WorkflowDictionaryType;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.model.workflow.entity.WorkflowTransitionValidation;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTransitionValidationDtoConstructor extends
		DtoConstructor<WorkflowTransitionValidation, WorkflowTransitionValidationDto> {

	private static final String DMN_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<definitions xmlns=\"http://www.omg.org/spec/DMN/20151101/dmn.xsd\" id=\"taskDecisions\" name=\"Task Decisions\" namespace=\"http://camunda.org/schema/1.0/dmn\">\n"
			+
			"  <decision id=\"taskValidation\" name=\"Проверка возможности перехода\">\n" +
			"    <decisionTable hitPolicy=\"COLLECT\">\n" +
			"      <output id=\"output_1\" label=\"Сообщение\" name=\"message\" typeRef=\"string\" />\n" +
			"    </decisionTable>\n" +
			"  </decision>\n" +
			"</definitions>";

	public WorkflowTransitionValidationDtoConstructor() {
		super(WorkflowTransitionValidation.class, WorkflowTransitionValidationDto.class);
	}

	@Override
	protected Map<DtoField<? super WorkflowTransitionValidationDto, ?>, ValueSupplier<? super WorkflowTransitionValidation, ? super WorkflowTransitionValidationDto, ?>> buildValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowTransitionValidationDto, ?>, ValueSupplier<? super WorkflowTransitionValidation, ? super WorkflowTransitionValidationDto, ?>>builder()
				.put(seq, (mapping, entity) -> entity.getSeq())
				.put(validCd, (mapping, entity) -> WorkflowDictionaryType.WF_TRN_DATA_VAL.lookupValue(entity.getValidCd()))
				.put(validCdKey, (mapping, entity) -> ofNullable(entity.getValidCd())
						.map(LOV::getKey)
						.orElse(null)
				)
				.put(errorMessage, (mapping, entity) -> entity.getErrorMessage())
				.put(dmn, (mapping, entity) -> entity.getDmn() == null ? DMN_TEMPLATE : entity.getDmn())
				.put(preInvokeType, (mapping, entity) -> WorkflowDictionaryType.PRE_INVOKE_TYPE.lookupValue(
						entity.getPreInvokeTypeCd()
				))
				.put(preInvokeCond, (mapping, entity) -> WorkflowDictionaryType.PRE_INVOKE_COND.lookupValue(
						entity.getPreInvokeCondCd()
				))
				.put(preInvokeMessage, (mapping, entity) -> entity.getPreInvokeMessage())
				.build();
	}

}
