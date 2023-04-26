/*-
 * #%L
 * IO Cxbox - Dictionary Links Model
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

package org.cxbox.model.dictionary.links.entity;

import org.cxbox.model.core.entity.BaseEntity;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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

	private boolean allValues;

	private boolean filterableField;

	private boolean defaultRuleFlg;

	@ManyToOne
	@JoinColumn(name = "SERVICE_ID")
	private CustomizableResponseService service;

	@OneToMany(mappedBy = "dictionaryLnkRule", cascade = {CascadeType.DETACH})
	private Set<DictionaryLnkRuleValue> values;

	@OneToMany(mappedBy = "dictionaryLnkRule", cascade = {CascadeType.DETACH})
	private Set<DictionaryLnkRuleCond> conditions;

}
