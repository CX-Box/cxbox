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

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.dict.WorkflowDictionaryType;
import org.cxbox.core.dict.WorkflowLov;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowTransitionValidationDto extends DataResponseDTO {

	private Long seq;

	private String validCd;

	private String validCdKey;

	private String errorMessage;

	private String dmn;

	@WorkflowLov(WorkflowDictionaryType.PRE_INVOKE_TYPE)
	private String preInvokeType;

	@WorkflowLov(WorkflowDictionaryType.PRE_INVOKE_COND)
	private String preInvokeCond;

	private String preInvokeMessage;

}
