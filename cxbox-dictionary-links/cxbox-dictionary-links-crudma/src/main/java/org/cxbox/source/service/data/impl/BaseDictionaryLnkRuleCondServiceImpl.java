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

import static org.cxbox.api.data.dictionary.CoreDictionaries.DictionaryTermType.DICTIONARY_FIELD;

import lombok.SneakyThrows;
import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.api.data.dictionary.DictionaryType;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.DTOUtils;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRule;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleCond;
import org.cxbox.source.dto.DictionaryLnkRuleCondDto;
import org.cxbox.source.dto.DictionaryLnkRuleCondDto_;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class BaseDictionaryLnkRuleCondServiceImpl<D extends DictionaryLnkRuleCondDto, E extends DictionaryLnkRuleCond>
		extends VersionAwareResponseService<D, E> {

	@Autowired
	private DictionaryCache dictionaryCache;

	public BaseDictionaryLnkRuleCondServiceImpl(Class<D> typeOfDTO, Class<E> typeOfEntity,
			SingularAttribute<? super E, ? extends BaseEntity> parentSpec,
			Class<? extends FieldMetaBuilder<D>> metaBuilder) {
		super(typeOfDTO, typeOfEntity, parentSpec, metaBuilder);
	}

	@Override
	protected CreateResult<D> doCreateEntity(final E entity, final BusinessComponent bc) {
		entity.setDictionaryLnkRule(baseDAO.findById(DictionaryLnkRule.class, bc.getParentIdAsLong()));
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	protected final ActionResultDTO<D> doUpdateEntity(E entity, D data, BusinessComponent bc) {
		boolean isSqlService = "SqlCrudmaService"
				.equals(entity.getDictionaryLnkRule().getService().getServiceName());
		if (data.hasChangedFields()) {
			if (data.isFieldChanged(DictionaryLnkRuleCondDto_.type)) {
				entity.setType(DictionaryType.DICTIONARY_TERM_TYPE.lookupName(data.getType()));
				entity.setFieldName(null);
				entity.setDepartmentId(null);
				entity.setFieldTextValue(null);
				entity.setBcName(null);
				entity.setFieldDictValue(null);
			}
			if (data.isFieldChanged(DictionaryLnkRuleCondDto_.ruleInversionFlg)) {
				entity.setRuleInversionFlg(data.isRuleInversionFlg());
			}
		}
		return doUpdateEntity(entity, data, isSqlService, bc);
	}


	@SneakyThrows
	protected ActionResultDTO<D> doUpdateEntity(E entity, D data, boolean isSqlService, BusinessComponent bc) {
		if (data.hasChangedFields()) {
			if (data.isFieldChanged(DictionaryLnkRuleCondDto_.fieldName)) {
				entity.setFieldName(data.getFieldName());
				Class<?> dtoClass = Class.forName(entity.getDictionaryLnkRule().getService().getDtoClass());
				if (DICTIONARY_FIELD.equals(entity.getType()) && !isSqlService) {
					entity.setFieldType(DTOUtils.getDictionaryType(dtoClass, data.getFieldName()));
				}
			}
			if (data.isFieldChanged(DictionaryLnkRuleCondDto_.bcName)) {
				entity.setBcName(data.getBcName());
			}
			if (data.isFieldChanged(DictionaryLnkRuleCondDto_.departmentId)) {
				entity.setDepartmentId(Long.valueOf(data.getDepartmentId()));
			}
			if (data.isFieldChanged(DictionaryLnkRuleCondDto_.fieldTextValue)) {
				entity.setFieldTextValue(data.getFieldTextValue());
			}
			if (data.isFieldChanged(DictionaryLnkRuleCondDto_.fieldDictValue)) {
				entity.setFieldDictValue(
						entity.getFieldType() == null ? null
								: dictionaryCache.lookupName(data.getFieldDictValue(), entity.getFieldType())
				);
			}
			if (data.isFieldChanged(DictionaryLnkRuleCondDto_.fieldType) && isSqlService) {
				entity.setFieldType(data.getFieldType());
				entity.setFieldDictValue(null);
			}
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	@Override
	public Actions<D> getActions() {
		return Actions.<D>builder()
				.create().add()
				.save().add()
				.delete().add()
				.build();
	}

}
