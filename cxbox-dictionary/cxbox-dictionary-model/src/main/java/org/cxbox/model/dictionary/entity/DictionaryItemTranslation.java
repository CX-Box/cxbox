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

import org.cxbox.model.core.api.Translation;
import org.cxbox.model.core.api.TranslationId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
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
@Table(name = "DICTIONARY_ITEM_TR")
public class DictionaryItemTranslation implements Translation<DictionaryItem, DictionaryItemTranslation> {

	@EmbeddedId
	private TranslationId translationId;

	@ManyToOne
	@MapsId("id")
	@JoinColumn(name = "id")
	private DictionaryItem primaryEntity;

	@Column(length = 500)
	private String value;

	@Override
	public DictionaryItemTranslation copyTranslation() {
		DictionaryItemTranslation copy = new DictionaryItemTranslation();
		copy.setValue(getValue());
		return copy;
	}

}
