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

package org.cxbox.core.util.filter.provider.impl;

import static org.cxbox.core.controller.param.SearchOperation.CONTAINS_ONE_OF;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS_ONE_OF;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DictionaryValueProvider extends AbstractClassifyDataProvider implements ClassifyDataProvider {

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper objectMapper;

	@Override
	protected List<ClassifyDataParameter> getProviderParameterValues(Field dtoField, ClassifyDataParameter dataParameter,
			FilterParameter filterParam, SearchParameter searchParam,
			List<ClassifyDataProvider> providers) {
		List<ClassifyDataParameter> result;
		Class<?> type = getDictType(dtoField);
		if (CONTAINS_ONE_OF.equals(dataParameter.getOperator()) || EQUALS_ONE_OF.equals(dataParameter.getOperator())) {
			dataParameter.setValue(filterParam.getStringValuesAsList().stream()
					.map(val -> convertDictToTargetType(objectMapper.convertValue(val, type)))
					.collect(Collectors.toList()));
		} else {
			dataParameter.setValue(convertDictToTargetType(objectMapper.convertValue(filterParam.getStringValue(), type)));
		}
		result = Collections.singletonList(dataParameter);
		return result;
	}

	public Object convertDictToTargetType(Object value) {
		return value;
	}

	private Class<?> getDictType(Field dtoField) {
		Class<?> dtoFieldType = dtoField.getType();
		Class<?> type;
		type = dtoFieldType;
		return type;
	}
}

