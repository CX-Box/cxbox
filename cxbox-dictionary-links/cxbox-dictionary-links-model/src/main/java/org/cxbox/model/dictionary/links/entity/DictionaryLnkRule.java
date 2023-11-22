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

package org.cxbox.model.dictionary.links.entity;

import jakarta.persistence.Convert;
import org.cxbox.model.core.entity.BaseEntity;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "DICTIONARY_LNK_RULE")
public class DictionaryLnkRule extends BaseEntity {

	private String field;

	private String name;

	private String type;

	@Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
	private Boolean allValues;

	@Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
	private Boolean filterableField;

	@Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
	private Boolean defaultRuleFlg;

	@ManyToOne
	@JoinColumn(name = "SERVICE_ID")
	private CustomizableResponseService service;

	@OneToMany(mappedBy = "dictionaryLnkRule", cascade = {CascadeType.DETACH})
	private Set<DictionaryLnkRuleValue> values;

	@OneToMany(mappedBy = "dictionaryLnkRule", cascade = {CascadeType.DETACH})
	private Set<DictionaryLnkRuleCond> conditions;

}
