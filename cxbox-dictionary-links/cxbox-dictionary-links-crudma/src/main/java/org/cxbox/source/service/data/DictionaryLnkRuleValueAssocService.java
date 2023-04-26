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

package org.cxbox.source.service.data;

import org.cxbox.core.service.ResponseService;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleValue;
import org.cxbox.source.dto.DictionaryLnkRuleValueDto;


public interface DictionaryLnkRuleValueAssocService extends
		ResponseService<DictionaryLnkRuleValueDto, DictionaryLnkRuleValue> {

}
