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

package org.cxbox.source.engine.condition;

import org.cxbox.api.data.dictionary.CoreDictionaries.DictionaryTermType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.source.engine.ResponseServiceExtractor;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleCond;
import org.cxbox.source.engine.LinkedDictionaryConditionChecker;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("TextFieldLinkedDictionaryConditionChecker")
public class TextFieldLinkedDictionaryConditionChecker implements LinkedDictionaryConditionChecker<String> {

	private final ResponseServiceExtractor responseServiceExtractor;

	@Override
	public LOV getType() {
		return DictionaryTermType.TEXT_FIELD;
	}

	@Override
	public boolean check(String object, DictionaryLnkRuleCond ruleCond) {
		return object != null && Objects.equals(object, ruleCond.getFieldTextValue());
	}

	@Override
	public boolean accept(DictionaryLnkRuleCond ruleCond, BusinessComponent bc) {
		return ruleCond.getFieldName() != null && ruleCond.getFieldTextValue() != null;
	}

	@Override
	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
			cacheNames = CacheConfig.REQUEST_CACHE,
			keyGenerator = "conditionKeyGenerator"
	)
	public String prepare(DictionaryLnkRuleCond ruleCond, BusinessComponent bc) {
		return (String) responseServiceExtractor.getFieldValue(bc, ruleCond.getFieldName());
	}

}
