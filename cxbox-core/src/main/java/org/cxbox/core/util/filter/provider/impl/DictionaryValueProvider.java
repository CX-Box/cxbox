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
import org.cxbox.dictionary.Dictionary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * {@link ClassifyDataProvider} for dictionary-typed DTO fields. Converts string filter values into the
 * dictionary type (resolved from the field type or its {@link BaseDictionary @BaseDictionary} annotation)
 * <p>
 * For use with {@link org.cxbox.core.dto.multivalue.MultivalueField MultivalueField} the field must be annotated with
 * {@link BaseDictionary @BaseDictionary}.
 * <p>
 * Example:
 * <pre>{@code
 * // Dictionary
 * public record TestDict(String key) implements Dictionary {
 *   public static final TestDict VALUE_1 = new TestDict("value 1");
 *   public static final TestDict VALUE_2 = new TestDict("value 2");
 * }
 *
 * // Entity
 * public class Entity extends BaseEntity {
 *
 *   @Column(name = "TEST_DICT")
 *   public TestDict testDict;
 *
 *   @ElementCollection(targetClass = TestDict.class)
 *   @CollectionTable(name = "TEST_DICT_TO_ENTITY", joinColumns = @JoinColumn(name = "ENTITY_ID"))
 *   @Column(name = "TEST_DICT", nullable = false)
 *   public Set<TestDict> testDicts = new HashSet<>();
 * }
 *
 * // DTO
 * public class EntityDTO extends DataResponseDTO {
 *   //!WARN. Must be annotated BaseDictionary
 *   @BaseDictionary(TestDict.class)
 *   @SearchParameter(name = "testDictCollection", provider = MultiFieldValueProvider.class, multiFieldKey = DictionaryValueProvider.class)
 *   private MultivalueField testDictCollection;
 *
 *   @SearchParameter(name = "testDict", provider = DictionaryValueProvider.class)
 *   private TestDict testDict = TestDict.VALUE_1;
 * }
 * }</pre>
 *
 * @see BaseDictionary
 * @see AbstractClassifyDataProvider
 *
 */
@Component
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DictionaryValueProvider extends AbstractClassifyDataProvider implements ClassifyDataProvider {

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper objectMapper;

	/**
	 * Converts the filter's string value(s) into the resolved dictionary type and stores them on {@code dataParameter}.
	 *
	 * @param dtoField the field being filtered
	 * @param dataParameter parameter populated with the converted value(s)
	 * @param filterParam source of the raw string value(s)
	 * @return singleton list containing {@code dataParameter}
	 */
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


	/**
	 * Extracts the dictionary class declared by the field's {@link BaseDictionary} annotation, if present.
	 *
	 * @return the annotated dictionary class, or empty if the field is not annotated with {@link BaseDictionary}.
	 */
	private Optional<Class<? extends Dictionary>> getType(Field field) {
		BaseDictionary annotation = field.getAnnotation(BaseDictionary.class);
		if (annotation != null) {
			return Optional.of(annotation.value());
		}
		return Optional.empty();
	}


	public Object convertDictToTargetType(Object value) {
		return value;
	}

	private Class<?> getDictType(Field dtoField) {
		Class<?> dtoFieldType = dtoField.getType();
		Class<?> type;
		if (Dictionary.class.isAssignableFrom(dtoFieldType)) {
			type = dtoFieldType;
		} else {
			type = getType(dtoField).orElseThrow(() -> new ServerException(
					"DictionaryValueProvider must be used with Dictionary dto field or field annotated with @BaseDictionary"));
		}
		return type;
	}

	/**
	 * Used when a JPA entity enum field is mapped to
	 * a {@link org.cxbox.api.data.dto.DataResponseDTO DataResponseDTO} field which type is not dictionary.
	 * Necessary to define the type of field by which filtering will be performed.
	 */
	@Target(FIELD)
	@Retention(RUNTIME)
	public @interface BaseDictionary {

		/**
		 * @return Class of the corresponding dictionary field in JPA entity.
		 */
		Class<? extends Dictionary> value();

	}
}

