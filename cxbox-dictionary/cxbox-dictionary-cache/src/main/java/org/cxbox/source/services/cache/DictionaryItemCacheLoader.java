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

package org.cxbox.source.services.cache;

import org.cxbox.api.data.dictionary.DictionaryCacheLoader;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.dictionary.entity.DictionaryItem;
import org.cxbox.model.dictionary.entity.DictionaryItemTranslation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DictionaryItemCacheLoader implements DictionaryCacheLoader {

	private final JpaDao jpaDao;

	public DictionaryItemCacheLoader(JpaDao jpaDao) {
		this.jpaDao = jpaDao;
	}

	@Override
	public String getLoaderName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public List<SimpleDictionary> load() {
		return jpaDao.getList(DictionaryItemTranslation.class).stream()
				.map(this::toDictionary).collect(Collectors.toList());
	}

	private SimpleDictionary toDictionary(DictionaryItemTranslation translation) {
		SimpleDictionary simpleDictionary = new SimpleDictionary();
		DictionaryItem dictionaryItem = translation.getPrimaryEntity();
		simpleDictionary.setType(dictionaryItem.getType());
		simpleDictionary.setKey(dictionaryItem.getKey());
		simpleDictionary.setValue(translation.getValue());
		simpleDictionary.setActive(dictionaryItem.isActive());
		simpleDictionary.setDescription(dictionaryItem.getDescription());
		simpleDictionary.setLanguage(translation.getLanguage());
		simpleDictionary.setDisplayOrder(dictionaryItem.getDisplayOrder());
		return simpleDictionary;
	}


}
