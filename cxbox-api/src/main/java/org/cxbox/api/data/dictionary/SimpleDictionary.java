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

package org.cxbox.api.data.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cxbox.dictionary.DictionaryProvider.DictionaryValue;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDictionary implements DictionaryValue {

	private String type;

	private String key;

	private String value;

	private String description;

	private String language;

	private Integer displayOrder;

	private boolean active;

	private String cacheLoaderName;

	public SimpleDictionary(String key, String value) {
		this(key, value, true);
	}

	public SimpleDictionary(String key, String value, boolean active) {
		this.key = key;
		this.value = value;
		this.active = active;
	}

}
