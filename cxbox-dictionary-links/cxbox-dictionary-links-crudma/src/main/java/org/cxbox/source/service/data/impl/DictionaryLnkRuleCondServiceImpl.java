/*-
 * #%L
 * IO Cxbox - Dictionary Links Implementation
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

package org.cxbox.source.service.data.impl;

import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleCond;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleCond_;
import org.cxbox.source.dto.DictionaryLnkRuleCondDto;
import org.cxbox.source.service.data.DictionaryLnkRuleCondService;
import org.cxbox.source.service.meta.DictionaryLnkRuleCondFieldMetaBuilder;
import org.springframework.stereotype.Service;

@Service
public class DictionaryLnkRuleCondServiceImpl extends
		BaseDictionaryLnkRuleCondServiceImpl<DictionaryLnkRuleCondDto, DictionaryLnkRuleCond>
		implements DictionaryLnkRuleCondService {

	public DictionaryLnkRuleCondServiceImpl() {
		super(
				DictionaryLnkRuleCondDto.class,
				DictionaryLnkRuleCond.class,
				DictionaryLnkRuleCond_.dictionaryLnkRule,
				DictionaryLnkRuleCondFieldMetaBuilder.class
		);
	}


}
