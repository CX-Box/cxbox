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

package org.cxbox.source.service.meta;

import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.source.dto.DictionaryLnkRuleValueDto;
import org.cxbox.source.dto.DictionaryLnkRuleValueDto_;
import org.springframework.stereotype.Service;

@Service
public class DictionaryLnkRuleValueFieldMetaBuilder extends FieldMetaBuilder<DictionaryLnkRuleValueDto> {

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<DictionaryLnkRuleValueDto> fields,
			InnerBcDescription bcDescription, Long id,
			Long parentId) {
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<DictionaryLnkRuleValueDto> fields, InnerBcDescription bcDescription,
			Long parentId) {
		fields.enableFilter(DictionaryLnkRuleValueDto_.value, DictionaryLnkRuleValueDto_.valueCd);
	}

}
