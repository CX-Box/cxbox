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
import org.cxbox.core.util.filter.provider.impl.BooleanValueProvider;
import org.cxbox.model.dictionary.entity.DictionaryItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryItemDTO extends DataResponseDTO {

	@SearchParameter
	private String key;

	@SearchParameter
	private String value;

	@SearchParameter(provider = BooleanValueProvider.class)
	private Boolean active;

	@SearchParameter
	private String type;

	private Integer displayOrder;

	private String description;

	@SearchParameter(provider = BooleanValueProvider.class)
	private boolean additionFlg;

	public DictionaryItemDTO(DictionaryItem dictionaryItem) {
		this.id = dictionaryItem.getId().toString();
		this.key = dictionaryItem.getKey();
		this.active = dictionaryItem.isActive();
		this.type = dictionaryItem.getType();
		this.displayOrder = dictionaryItem.getDisplayOrder();
		this.description = dictionaryItem.getDescription();
		this.additionFlg = dictionaryItem.getAdditionFlg();
	}

}
