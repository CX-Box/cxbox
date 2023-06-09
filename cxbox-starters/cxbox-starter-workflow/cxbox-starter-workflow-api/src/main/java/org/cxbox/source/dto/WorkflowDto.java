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

import org.cxbox.api.data.dictionary.DictionaryType;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.dto.Lov;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.impl.LovValueProvider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowDto extends DataResponseDTO {

	@SearchParameter
	private String name;

	@SearchParameter
	private String description;

	@Lov(DictionaryType.TASK_TYPE)
	@SearchParameter(provider = LovValueProvider.class)
	private String taskTypeCd;

	private Long deptId;

	@SearchParameter(name = "department.shortName")
	private String deptShortName;

	private Long activeVersionId;

	private String activeVersion;

}
