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

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import org.apache.commons.lang3.math.NumberUtils;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.DTOUtils;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.service.action.Actions;
import org.cxbox.api.util.CxReflectionUtils;
import org.cxbox.model.dictionary.links.entity.CustomizableResponseService;
import org.cxbox.model.dictionary.links.entity.CustomizableResponseService_;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRule;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleCond;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleCond_;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleValue;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleValue_;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRule_;
import org.cxbox.source.dto.DictionaryLnkRuleDto;
import org.cxbox.source.dto.DictionaryLnkRuleDto_;
import org.cxbox.source.service.data.DictionaryLnkRuleService;
import org.cxbox.source.service.meta.DictionaryLnkRuleFieldMetaBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class DictionaryLnkRuleServiceImpl extends
		VersionAwareResponseService<DictionaryLnkRuleDto, DictionaryLnkRule> implements DictionaryLnkRuleService {

	public DictionaryLnkRuleServiceImpl() {
		super(DictionaryLnkRuleDto.class, DictionaryLnkRule.class, null, DictionaryLnkRuleFieldMetaBuilder.class);
	}

	@Override
	protected Specification<DictionaryLnkRule> getSpecification(BusinessComponent bc) {
		return (root, cq, cb) ->
				cb.equal(
						root.get(DictionaryLnkRule_.service).get(CustomizableResponseService_.id),
						NumberUtils.createLong(bc.getParentId())
				);
	}


	@Override
	protected CreateResult<DictionaryLnkRuleDto> doCreateEntity(final DictionaryLnkRule entity,
			final BusinessComponent bc) {
		entity.setService(baseDAO.findById(CustomizableResponseService.class, bc.getParentIdAsLong()));
		Long id = baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, baseDAO.findById(DictionaryLnkRule.class, id)));
	}

	@Override
	protected ActionResultDTO<DictionaryLnkRuleDto> doUpdateEntity(DictionaryLnkRule entity, DictionaryLnkRuleDto data,
			BusinessComponent bc) {
		boolean needChildDeletion = false;
		boolean isSqlService = "SqlCrudmaService".equals(entity.getService().getServiceName());
		if (data.hasChangedFields()) {
			if (data.isFieldChanged(DictionaryLnkRuleDto_.name)) {
				entity.setName(data.getName());
			}
			if (data.isFieldChanged(DictionaryLnkRuleDto_.filterableField)) {
				entity.setFilterableField(data.getFilterableField());
			}
			if (data.isFieldChanged(DictionaryLnkRuleDto_.allValues)) {
				entity.setAllValues(data.getAllValues());
				if (entity.getAllValues()) {
					needChildDeletion = true;
				}
			}
			if (data.isFieldChanged(DictionaryLnkRuleDto_.field)) {
				if (!entity.getValues().isEmpty()) {
					throw new BusinessException()
							.addPopup(errorMessage("error.cant_modify_rule_field_values_exist"));
				}
				entity.setField(data.getField());
				Class<?> dtoClass = CxReflectionUtils.forName(entity.getService().getDtoClass());
				if (!isSqlService) {
					entity.setType(DTOUtils.getDictionaryType(dtoClass, data.getField()));
				}
			}
			if (data.isFieldChanged(DictionaryLnkRuleDto_.defaultRuleFlg)) {
				entity.setDefaultRuleFlg(data.getDefaultRuleFlg());
				if (entity.getDefaultRuleFlg()) {
					needChildDeletion = true;
				}
			}
			if (data.isFieldChanged(DictionaryLnkRuleDto_.type) && isSqlService) {
				entity.setType(data.getType());
			}

			if (needChildDeletion) {
				baseDAO.getList(DictionaryLnkRuleCond.class, (root, cq, cb) ->
						cb.equal(root.get(DictionaryLnkRuleCond_.dictionaryLnkRule), entity)
				).forEach(baseDAO::delete);
			}
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	@Override
	public ActionResultDTO<DictionaryLnkRuleDto> deleteEntity(BusinessComponent bc) {
		DictionaryLnkRule entity = baseDAO.findById(DictionaryLnkRule.class, bc.getIdAsLong());
		baseDAO.getList(DictionaryLnkRuleValue.class, (root, cq, cb) ->
				cb.equal(root.get(DictionaryLnkRuleValue_.dictionaryLnkRule), entity)
		).forEach(baseDAO::delete);
		baseDAO.getList(DictionaryLnkRuleCond.class, (root, cq, cb) ->
				cb.equal(root.get(DictionaryLnkRuleCond_.dictionaryLnkRule), entity)
		).forEach(baseDAO::delete);
		baseDAO.delete(entity);
		return new ActionResultDTO<>();
	}

	@Override
	public Actions<DictionaryLnkRuleDto> getActions() {
		return Actions.<DictionaryLnkRuleDto>builder()
				.create().add()
				.save().add()
				.delete().add()
				.build();
	}

}
