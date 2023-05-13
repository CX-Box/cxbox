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
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.model.dictionary.entity.DictionaryTypeDesc;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryTypeDescDTO extends DataResponseDTO {

	@SearchParameter
	private String type;

	@SearchParameter
	private String typeDesc;

	public DictionaryTypeDescDTO(DictionaryTypeDesc dictionaryTypeDesc) {
		this.id = dictionaryTypeDesc.getId().toString();
		this.type = dictionaryTypeDesc.getType();
		this.typeDesc = dictionaryTypeDesc.getTypeDesc();
	}

}
