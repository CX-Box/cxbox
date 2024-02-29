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

import static org.cxbox.api.data.dictionary.CoreDictionaries.DictionaryTermType.BC;
import static org.cxbox.api.data.dictionary.CoreDictionaries.DictionaryTermType.BOOLEAN_FIELD;
import static org.cxbox.api.data.dictionary.CoreDictionaries.DictionaryTermType.DEPT;
import static org.cxbox.api.data.dictionary.CoreDictionaries.DictionaryTermType.DICTIONARY_FIELD;
import static org.cxbox.api.data.dictionary.CoreDictionaries.DictionaryTermType.FIELD_IS_EMPTY;
import static org.cxbox.api.data.dictionary.CoreDictionaries.DictionaryTermType.TEXT_FIELD;
import static org.cxbox.api.data.dictionary.DictionaryType.DICTIONARY_TERM_TYPE;

import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.DTOUtils;
import org.cxbox.core.dto.LovUtils;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.core.util.InstrumentationAwareReflectionUtils;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleCond;
import org.cxbox.source.dto.DictionaryLnkRuleCondDto;
import org.cxbox.source.dto.DictionaryLnkRuleCondDto_;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class BaseDictionaryLnkRuleCondFieldMetaBuilder<R extends DictionaryLnkRuleCondDto>
		extends FieldMetaBuilder<R> {

	@Autowired
	private JpaDao jpaDao;

	@Autowired
	private DictionaryCache dictionaryCache;

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<R> fields,
			InnerBcDescription bcDescription, Long id,
			Long parentId) {
		fields.setEnabled(DictionaryLnkRuleCondDto_.type, DictionaryLnkRuleCondDto_.ruleInversionFlg);
		if (id == null) {
			return;
		}
		DictionaryLnkRuleCond entity = jpaDao.findById(DictionaryLnkRuleCond.class, id);
		if (entity == null) {
			return;
		}
		boolean isSqlService = "SqlCrudmaService"
				.equals(entity.getDictionaryLnkRule().getService().getServiceName());
		if (DEPT.equals(entity.getType())) {
			fields.setEnabled(DictionaryLnkRuleCondDto_.departmentId);
		} else if (FIELD_IS_EMPTY.equals(entity.getType())) {
			Class<?> dtoClass = InstrumentationAwareReflectionUtils.forName(entity.getDictionaryLnkRule().getService().getDtoClass());
			List<SimpleDictionary> dictDTOList = InstrumentationAwareReflectionUtils.getFields(dtoClass).stream()
					.map(Field::getName).map(field -> new SimpleDictionary(field, field)).collect(Collectors.toList());
			fields.setConcreteValues(DictionaryLnkRuleCondDto_.fieldName, dictDTOList);
			fields.setEnabled(DictionaryLnkRuleCondDto_.fieldName);
		} else if (TEXT_FIELD.equals(entity.getType())) {
			Class<?> dtoClass = InstrumentationAwareReflectionUtils.forName(entity.getDictionaryLnkRule().getService().getDtoClass());
			List<SimpleDictionary> dictDTOList = InstrumentationAwareReflectionUtils.getFields(dtoClass).stream()
					.filter(field -> (LovUtils.getType(field) == null && field.getType().equals(String.class)) && !field
							.getName().contains("edit_lov"))
					.map(Field::getName).map(field -> new SimpleDictionary(field, field)).collect(Collectors.toList());
			fields.setConcreteValues(DictionaryLnkRuleCondDto_.fieldName, dictDTOList);
			fields.setEnabled(DictionaryLnkRuleCondDto_.fieldName, DictionaryLnkRuleCondDto_.fieldTextValue);
		} else if (DICTIONARY_FIELD.equals(entity.getType())) {
			fields.setEnabled(DictionaryLnkRuleCondDto_.fieldName);
			if (isSqlService) {
				List<SimpleDictionary> allTypes = dictionaryCache.types().stream()
						.map(type -> new SimpleDictionary(type, type)).collect(
								Collectors.toList());
				fields.setEnabled(DictionaryLnkRuleCondDto_.fieldType);
				fields.setConcreteValues(DictionaryLnkRuleCondDto_.fieldType, allTypes);
				if (entity.getFieldType() != null) {
					fields.setEnabled(DictionaryLnkRuleCondDto_.fieldDictValue);
					fields.setDictionaryTypeWithAllValues(
							DictionaryLnkRuleCondDto_.fieldDictValue,
							entity.getFieldType()
					);
				}
			}
			Class<?> dtoClass = InstrumentationAwareReflectionUtils.forName(entity.getDictionaryLnkRule().getService().getDtoClass());
			List<SimpleDictionary> dictDTOList = InstrumentationAwareReflectionUtils.getFields(dtoClass).stream()
					.filter(field -> (LovUtils.getType(field) != null && field.getType().equals(String.class)) || (
							isSqlService && field.getName().contains("edit_lov")))
					.map(Field::getName).map(field -> new SimpleDictionary(field, field)).collect(Collectors.toList());
			fields.setConcreteValues(DictionaryLnkRuleCondDto_.fieldName, dictDTOList);
			if (entity.getFieldName() != null && !isSqlService) {
				fields.setEnabled(DictionaryLnkRuleCondDto_.fieldDictValue);
				String dictionaryType = DTOUtils.getDictionaryType(dtoClass, entity.getFieldName());
				if (dictionaryType != null) {
					fields.setDictionaryTypeWithAllValues(DictionaryLnkRuleCondDto_.fieldDictValue, dictionaryType);
				}
			}
		} else if (BOOLEAN_FIELD.equals(entity.getType())) {
			Class<?> dtoClass = InstrumentationAwareReflectionUtils.forName(entity.getDictionaryLnkRule().getService().getDtoClass());
			List<SimpleDictionary> dictDTOList = InstrumentationAwareReflectionUtils.getFields(dtoClass).stream()
					.filter(field -> field.getType().equals(Boolean.class) || field.getType().equals(boolean.class))
					.map(Field::getName).map(field -> new SimpleDictionary(field, field)).collect(Collectors.toList());
			fields.setConcreteValues(DictionaryLnkRuleCondDto_.fieldName, dictDTOList);
			fields.setEnabled(DictionaryLnkRuleCondDto_.fieldName, DictionaryLnkRuleCondDto_.fieldBooleanValue);
		} else if (BC.equals(entity.getType())) {
			fields.setEnabled(DictionaryLnkRuleCondDto_.bcName);
		}
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<R> fields, InnerBcDescription bcDescription, Long parentId) {
		fields.enableFilter(DictionaryLnkRuleCondDto_.type, DictionaryLnkRuleCondDto_.fieldName);
		fields.setForceActive(
				DictionaryLnkRuleCondDto_.type,
				DictionaryLnkRuleCondDto_.fieldName,
				DictionaryLnkRuleCondDto_.fieldType
		);
		fields.setAllFilterValuesByLovType(DictionaryLnkRuleCondDto_.type, DICTIONARY_TERM_TYPE);
		fields.setDictionaryTypeWithAllValues(DictionaryLnkRuleCondDto_.type, DICTIONARY_TERM_TYPE);
	}

}
