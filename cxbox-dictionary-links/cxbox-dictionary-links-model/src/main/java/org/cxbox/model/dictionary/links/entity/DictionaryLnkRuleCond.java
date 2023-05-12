
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

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.core.entity.Department;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "DICTIONARY_LNK_RULE_COND")
public class DictionaryLnkRuleCond extends BaseEntity {

	private LOV type;

	private String fieldName;

	private String fieldTextValue;

	private Boolean fieldBooleanValue;

	private LOV fieldDictValue;

	private String fieldType;

	private boolean ruleInversionFlg;

	@ManyToOne
	@JoinColumn(name = "DEPT_ID")
	private Department department;

	private String bcName;

	@ManyToOne
	@JoinColumn(name = "RULE_ID", nullable = false)
	private DictionaryLnkRule dictionaryLnkRule;

}
