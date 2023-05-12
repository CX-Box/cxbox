
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

package org.cxbox.source.service.data.impl;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.data.dto.AssociateDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.AbstractResponseService;
import org.cxbox.core.dto.rowmeta.AssociateResultDTO;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.core.service.action.Actions;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRule;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleValue;
import org.cxbox.source.dto.DictionaryLnkRuleValueDto;
import org.cxbox.source.service.data.DictionaryLnkRuleValueService;
import org.cxbox.source.service.meta.DictionaryLnkRuleValueFieldMetaBuilder;
import org.cxbox.source.service.specification.DictionaryLnkRuleValueLinkSpecifications;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DictionaryLnkRuleValueServiceImpl extends
		AbstractResponseService<DictionaryLnkRuleValueDto, DictionaryLnkRuleValue> implements
		DictionaryLnkRuleValueService {

	public DictionaryLnkRuleValueServiceImpl() {
		super(
				DictionaryLnkRuleValueDto.class,
				DictionaryLnkRuleValue.class,
				null,
				DictionaryLnkRuleValueFieldMetaBuilder.class
		);
		this.linkSpecificationHolder = DictionaryLnkRuleValueLinkSpecifications.class;
	}

	@Override
	public Actions<DictionaryLnkRuleValueDto> getActions() {
		return Actions.<DictionaryLnkRuleValueDto>builder()
				.associate().add()
				.delete().add()
				.build();
	}

	@Override
	protected AssociateResultDTO doAssociate(List<AssociateDTO> data, BusinessComponent bc) {
		DictionaryLnkRule parent = baseDAO.findById(DictionaryLnkRule.class, bc.getParentIdAsLong());
		for (AssociateDTO dto : data) {
			if (dto.getAssociated()) {
				DictionaryLnkRuleValue entity = new DictionaryLnkRuleValue();
				entity.setDictionaryLnkRule(parent);
				entity.setChildKey(new LOV(dto.getId()));
				baseDAO.save(entity);
			}
		}
		return new AssociateResultDTO(Collections.emptyList())
				.setAction(PostAction.refreshBc(bc));
	}

}
