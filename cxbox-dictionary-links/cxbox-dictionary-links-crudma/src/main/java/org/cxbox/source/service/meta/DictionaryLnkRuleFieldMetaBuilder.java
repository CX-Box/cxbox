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

import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.exception.ServerException;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.LovUtils;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.core.util.InstrumentationAwareReflectionUtils;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.dictionary.links.entity.CustomizableResponseService;
import org.cxbox.source.dto.DictionaryLnkRuleDto;
import org.cxbox.source.dto.DictionaryLnkRuleDto_;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DictionaryLnkRuleFieldMetaBuilder extends FieldMetaBuilder<DictionaryLnkRuleDto> {

	private final JpaDao jpaDao;

	private final DictionaryCache dictionaryCache;

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<DictionaryLnkRuleDto> fields,
			InnerBcDescription bcDescription, Long id, Long parentId) {
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<DictionaryLnkRuleDto> fields, InnerBcDescription bcDescription,
			Long parentId) {
		fields.setEnabled(
				DictionaryLnkRuleDto_.allValues,
				DictionaryLnkRuleDto_.filterableField,
				DictionaryLnkRuleDto_.field,
				DictionaryLnkRuleDto_.name,
				DictionaryLnkRuleDto_.defaultRuleFlg
		);
		fields.enableFilter(
				DictionaryLnkRuleDto_.allValues,
				DictionaryLnkRuleDto_.filterableField,
				DictionaryLnkRuleDto_.field,
				DictionaryLnkRuleDto_.name,
				DictionaryLnkRuleDto_.defaultRuleFlg
		);
		final CustomizableResponseService responseService = jpaDao.findById(CustomizableResponseService.class, parentId);
		boolean isSqlService = "SqlCrudmaService".equals(responseService.getServiceName());
		final List<SimpleDictionary> fieldValues = getFieldValues(responseService.getDtoClass(), isSqlService);
		if (!fieldValues.isEmpty()) {
			fields.setConcreteValues(DictionaryLnkRuleDto_.field, fieldValues);
			fields.setConcreteFilterValues(DictionaryLnkRuleDto_.field, fieldValues);
		}
		if (isSqlService) {
			List<SimpleDictionary> allTypes = dictionaryCache.types().stream()
					.map(type -> new SimpleDictionary(type, type)).collect(
							Collectors.toList());
			fields.enableFilter(DictionaryLnkRuleDto_.type);
			fields.setEnabled(DictionaryLnkRuleDto_.type);
			fields.setConcreteValues(DictionaryLnkRuleDto_.type, allTypes);
			fields.setConcreteFilterValues(DictionaryLnkRuleDto_.type, allTypes);
		}
	}

	private List<SimpleDictionary> getFieldValues(String dtoClassName, boolean isSqlService) {
		if (dtoClassName == null) {
			return Collections.emptyList();
		}
		try {
			return InstrumentationAwareReflectionUtils.getFields(InstrumentationAwareReflectionUtils.forName(dtoClassName)).stream()
					.filter(field -> isSqlService ? field.getName().contains("edit_lov")
							: LovUtils.getType(field) != null)
					.map(Field::getName)
					.map(field -> new SimpleDictionary(field, field))
					.collect(Collectors.toList());
		} catch (Exception ex) {
			throw new ServerException(ex.getLocalizedMessage(), ex);
		}
	}

}
