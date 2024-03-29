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

import static org.cxbox.api.data.dictionary.DictionaryCache.dictionary;
import static org.cxbox.api.data.dictionary.DictionaryType.DICTIONARY_TERM_TYPE;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.dto.Lov;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.impl.LovValueProvider;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleCond;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryLnkRuleCondDto extends DataResponseDTO {

	@SearchParameter(provider = LovValueProvider.class)
	@Lov(DICTIONARY_TERM_TYPE)
	private String type;

	private String typeCd;

	private String fieldName;

	@SearchParameter(name = "fieldName")
	private String fieldNameText;

	private String fieldTextValue;

	private Boolean fieldBooleanValue;

	private String fieldDictValue;

	private String departmentId;

	private String bcName;

	private String fieldType;

	private boolean defaultRuleFlg;

	private boolean ruleInversionFlg;

	public DictionaryLnkRuleCondDto(DictionaryLnkRuleCond entity) {
		this.id = entity.getId().toString();
		this.fieldName = entity.getFieldName();
		this.fieldNameText = entity.getFieldName();
		this.bcName = entity.getBcName();
		this.departmentId = Optional.of(entity).map(DictionaryLnkRuleCond::getDepartmentId)
				.map(Object::toString).orElse(null);
		this.fieldTextValue = entity.getFieldTextValue();
		this.fieldBooleanValue = entity.getFieldBooleanValue();
		this.type = DICTIONARY_TERM_TYPE.lookupValue(entity.getType());
		this.typeCd = Optional.of(entity).map(DictionaryLnkRuleCond::getType).map(LOV::getKey).orElse(null);
		this.fieldType = entity.getFieldType();
		if (entity.getFieldType() != null) {
			this.fieldDictValue = dictionary().lookupValue(entity.getFieldDictValue(), entity.getFieldType());
		}
		if (entity.getDictionaryLnkRule() != null) {
			defaultRuleFlg = entity.getDictionaryLnkRule().getDefaultRuleFlg();
		}
		this.ruleInversionFlg = entity.getRuleInversionFlg();
	}

}
