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


import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.cxbox.core.controller.param.SearchOperation.CONTAINS_ONE_OF;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS_ONE_OF;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.exception.ServerException;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EnumValueProvider extends AbstractClassifyDataProvider implements ClassifyDataProvider {

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper objectMapper;

	@Override
	protected List<ClassifyDataParameter> getProviderParameterValues(Field dtoField, ClassifyDataParameter dataParameter,
			FilterParameter filterParam, SearchParameter searchParam,
			List<ClassifyDataProvider> providers) {
		List<ClassifyDataParameter> result;
		Class<?> type = getEnumType(dtoField);
		if (CONTAINS_ONE_OF.equals(dataParameter.getOperator()) || EQUALS_ONE_OF.equals(dataParameter.getOperator())) {
			dataParameter.setValue(filterParam.getStringValuesAsList().stream()
					.map(val -> convertEnumToTargetType(objectMapper.convertValue(val, type)))
					.collect(Collectors.toList()));
		} else {
			dataParameter.setValue(convertEnumToTargetType(objectMapper.convertValue(filterParam.getStringValue(), type)));
		}
		result = Collections.singletonList(dataParameter);
		return result;
	}

	public Object convertEnumToTargetType(Object value) {
		return value;
	}

	private Class<?> getEnumType(Field dtoField) {
		Class<?> dtoFieldType = dtoField.getType();
		Class<?> type;
		if (Enum.class.isAssignableFrom(dtoFieldType)) {
			type = dtoFieldType;
		} else {
			type = getType(dtoField).orElseThrow(() -> new ServerException(
					"EnumValueProvider must be used with Enum dto field or field annotated with @BaseEnum"));
		}
		return type;
	}

	private Optional<Class<? extends Enum<?>>> getType(Field field) {
		BaseEnum annotation = field.getAnnotation(BaseEnum.class);
		if (annotation != null) {
			return Optional.of(annotation.value());
		}
		return Optional.empty();
	}

	/**
	 * Used when a JPA entity enum field is mapped to
	 * a {@link org.cxbox.api.data.dto.DataResponseDTO DataResponseDTO} field which type is not enum.
	 * Necessary to define the type of field by which filtering will be performed.
	 */
	@Target(FIELD)
	@Retention(RUNTIME)
	public @interface BaseEnum {

		/**
		 * @return Class of the corresponding enum field in JPA entity.
		 */
		Class<? extends Enum<?>> value();

	}

}

