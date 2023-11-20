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

package org.cxbox.model.dictionary.entity;

import org.cxbox.model.core.api.Translatable;
import org.cxbox.model.core.entity.BaseEntity;
import java.io.Serializable;
import java.util.Map;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Entity for simple dictionaries
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "DICTIONARY_ITEM")
public class DictionaryItem extends BaseEntity implements Translatable<DictionaryItem, DictionaryItemTranslation>,
		Serializable {

	@Column
	private String type;

	@Column
	private String key;

	@Column
	private boolean active;

	@Column
	private Integer displayOrder;

	@Column
	private String description;

	@Column(name = "ADDITION_FLG")
	private boolean additionFlg;

	@ManyToOne
	@JoinColumn(name = "DICTIONARY_TYPE_ID", nullable = false)
	private DictionaryTypeDesc dictionaryTypeId;

	@OneToMany(mappedBy = "primaryEntity",
			fetch = FetchType.LAZY,
			cascade = {
					CascadeType.DETACH,
					CascadeType.MERGE,
					CascadeType.PERSIST,
					CascadeType.REFRESH
			},
			orphanRemoval = true)
	@MapKey(name = "translationId.language")
	private Map<String, DictionaryItemTranslation> translations;

}
