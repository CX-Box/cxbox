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

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.dict.WorkflowDictionaryType;
import org.cxbox.core.dict.WorkflowLov;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.impl.LongValueProvider;
import org.cxbox.core.util.filter.provider.impl.LovValueProvider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowPostFunctionDto extends DataResponseDTO {

	@SearchParameter(provider = LongValueProvider.class)
	private Long seq;

	@WorkflowLov(WorkflowDictionaryType.WF_TRN_ACT)
	@SearchParameter(provider = LovValueProvider.class)
	private String actionCd;

	@SearchParameter(provider = LongValueProvider.class)
	private Long stepTerm;

}
