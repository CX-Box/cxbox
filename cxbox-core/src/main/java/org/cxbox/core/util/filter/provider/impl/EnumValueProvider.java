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

/**
 {@link ClassifyDataProvider} for enum-typed DTO fields. Converts string filter values into the
 * enum type (resolved from the field type or its {@link BaseEnum @BaseEnum}).
 * <p>
 * For use with {@link org.cxbox.core.dto.multivalue.MultivalueField MultivalueField} the field must be annotated with
 * {@link BaseEnum}.
 * <p>
 * Example:
 * <pre>{@code
 * // Enum
 * @Getter
 * @AllArgsConstructor
 * public enum TestEnum {
 *   VALUE_1("value 1"),
 *   VALUE_2("value 2"),
 *   VALUE_3("value 3");
 *
 *   @JsonValue
 *   private final String value;
 * }
 *
 * // Entity
 * public class Entity extends BaseEntity {
 *
 *   @Column(name = "TEST_ENUM")
 *   @Enumerated(EnumType.STRING)
 *   public TestEnum testEnum;
 *
 *   @ElementCollection(targetClass = TestEnum.class)
 *   @CollectionTable(name = "TEST_ENUM_TO_ENTITY", joinColumns = @JoinColumn(name = "ENTITY_ID"))
 *   @Column(name = "TEST_ENUM", nullable = false)
 *   @Enumerated(EnumType.STRING)
 *   public Set<TestEnum> testEnums = new HashSet<>();
 * }
 *
 * // DTO
 * public class EntityDTO extends DataResponseDTO {
 *    //!WARN. Must be annotated BaseEnum
 *   @BaseEnum(TestEnum.class)
 *   @SearchParameter(name = "testEnumCollection", provider = MultiFieldValueProvider.class, multiFieldKey = EnumValueProvider.class)
 *   private MultivalueField testEnumCollection;
 *
 *   @SearchParameter(name = "testEnum", provider = EnumValueProvider.class)
 *   private TestEnum testEnum = TestEnum.VALUE_1;
 * }
 * }</pre>
 *
 * @see BaseEnum
 * @see AbstractClassifyDataProvider
 *
 */
@Component
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EnumValueProvider extends AbstractClassifyDataProvider implements ClassifyDataProvider {

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper objectMapper;

	/**
	 * Converts the filter's string value(s) into the resolved enum type and stores them on {@code dataParameter}.
	 *
	 * @param dtoField      the field being filtered
	 * @param dataParameter parameter populated with the converted value(s)
	 * @param filterParam   source of the raw string value(s)
	 * @return singleton list containing {@code dataParameter}
	 */
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

	/**
	 * Resolves the enum class for the field: its own type if it is an enum, otherwise the {@link BaseEnum} value.
	 *
	 * @throws ServerException if the field is neither an enum nor annotated with {@link BaseEnum}
	 */
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

	/** @return the enum class declared by the field's {@link BaseEnum} annotation, or empty if absent. */
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
	 * see example on {@link EnumValueProvider} class javadoc
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

