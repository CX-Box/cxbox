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

package org.cxbox.core.util;

import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.exception.ServerException;
import org.cxbox.api.util.compare.CxComparatorUtils;
import org.cxbox.api.util.compare.common.Transformer;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.controller.param.SortParameter;
import org.cxbox.core.controller.param.SortParameters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

@UtilityClass
public final class ListPaging {

	public static <T extends DataResponseDTO> ResultPage<T> getResultPage(final List<T> list,
			final QueryParameters queryParameters) {
		Stream<T> stream = list.stream();
		for (FilterParameter parameter : queryParameters.getFilter()) {
			stream = stream.filter(createFilter(parameter));
		}
		if (!queryParameters.getSort().isEmpty()) {
			stream = stream.sorted(createSorted(queryParameters.getSort()));
		}
		final List<T> filteredList = stream.toList();

		long from = (long) queryParameters.getPageNumber() * (long) queryParameters.getPageSize();
		long to = from + queryParameters.getPageSize();
		if (to > filteredList.size()) {
			to = filteredList.size();
		}
		return new ResultPage<>(filteredList.subList((int) from, (int) to), filteredList.size() > to);
	}

	private static <T> Predicate<T> createFilter(FilterParameter parameter) {
		return switch (parameter.getOperation()) {
			case CONTAINS -> new PredicateContains<>(parameter);
			case SPECIFIED -> new PredicateSpecified<>(parameter);
			case EQUALS -> new PredicateEquals<>(parameter);
			case GREATER_THAN -> new PredicateGreaterThan<>(parameter);
			case GREATER_OR_EQUAL_THAN -> new PredicateGreaterOrEqualThan<>(parameter);
			case LESS_THAN -> new PredicateLessThan<>(parameter);
			case LESS_OR_EQUAL_THAN -> new PredicateLessOrEqualThan<>(parameter);
			case EQUALS_ONE_OF -> new PredicateEqualsOneOf<>(parameter);
			case CONTAINS_ONE_OF -> new PredicateContainsOneOf<>(parameter);
			default ->
					throw new ServerException(String.format("Operation \"%s\" is not supported", parameter.getOperation()));
		};
	}

	private static <T> Comparator<T> createSorted(SortParameters sort) {
		final List<SortParameter> sortedParameters = new ArrayList<>(sort.getParameters());
		sortedParameters.sort(CxComparatorUtils.transformedComparator(
				CxComparatorUtils.nullHighComparator(CxComparatorUtils.<Integer>naturalComparator()),
				SortParameter::getPriority
		));
		List<Comparator<T>> comparators = new ArrayList<>();
		for (SortParameter sortedParameter : sortedParameters) {
			comparators.add(createSorted(sortedParameter));
		}
		return CxComparatorUtils.chainedComparator(comparators);
	}

	private static <T, F extends Comparable<? super F>> Comparator<T> createSorted(SortParameter parameter) {
		FieldTransformer<T, F> fieldTransformer = new FieldTransformer<>(parameter.getName());
		return switch (parameter.getType()) {
			case ASC -> CxComparatorUtils.transformedComparator(
					CxComparatorUtils.nullHighComparator(CxComparatorUtils.<F>naturalComparator()),
					fieldTransformer
			);
			case DESC -> CxComparatorUtils.transformedComparator(
					CxComparatorUtils.nullHighComparator(CxComparatorUtils.reversedComparator(CxComparatorUtils.<F>naturalComparator())),
					fieldTransformer
			);
		};
	}

	private static <T> Object getValue(final T dto, final String fieldName) {
		try {
			return FieldUtils.getField(dto.getClass(), fieldName, true).get(dto);
		} catch (Exception e) {
			return null;
		}
	}

	private record FieldTransformer<T, F extends Comparable<? super F>>(String fieldName) implements Transformer<T, F> {

		@Override
		public F transform(T input) {
			Object value = getValue(input, fieldName);
			try {
				if (value instanceof Comparable<?> comp) {
					return (F) comp;
				}
			} catch (Exception e) {
				return null;
			}
			return null;
		}

	}

	private record PredicateContains<T>(FilterParameter parameter) implements Predicate<T> {

		@Override
		public boolean test(T dto) {
			Object value = getValue(dto, parameter.getName());
			return value instanceof String str && StringUtils.containsIgnoreCase(str, parameter.getStringValue());
		}

	}

	private record PredicateSpecified<T>(FilterParameter parameter) implements Predicate<T> {

		@Override
		public boolean test(T dto) {
			Object value = getValue(dto, parameter.getName());
			return BooleanUtils.isNotFalse(parameter.getBooleanValue()) ? Objects.nonNull(value) : Objects.isNull(value);
		}

	}

	private record PredicateEquals<T>(FilterParameter parameter) implements Predicate<T> {

		@Override
		public boolean test(T dto) {
			Object value = getValue(dto, parameter.getName());
			return value != null && Objects.equals(value, parameter.getValue(value.getClass()));
		}

	}

	private record PredicateGreaterThan<T>(FilterParameter parameter) implements Predicate<T> {

		@Override
		public boolean test(T dto) {
			Object value = getValue(dto, parameter.getName());
			return value instanceof Comparable comp && comp.compareTo(parameter.getValue(value.getClass())) > 0;
		}

	}

	private record PredicateGreaterOrEqualThan<T>(FilterParameter parameter) implements Predicate<T> {

		@Override
		public boolean test(T dto) {
			Object value = getValue(dto, parameter.getName());
			return value instanceof Comparable comp && comp.compareTo(parameter.getValue(value.getClass())) >= 0;
		}

	}

	private record PredicateLessThan<T>(FilterParameter parameter) implements Predicate<T> {

		@Override
		public boolean test(T dto) {
			Object value = getValue(dto, parameter.getName());
			return value instanceof Comparable comp && comp.compareTo(parameter.getValue(value.getClass())) < 0;
		}

	}

	private record PredicateLessOrEqualThan<T>(FilterParameter parameter) implements Predicate<T> {

		@Override
		public boolean test(T dto) {
			Object value = getValue(dto, parameter.getName());
			return value instanceof Comparable comp && comp.compareTo(parameter.getValue(value.getClass())) <= 0;
		}

	}

	private record PredicateEqualsOneOf<T>(FilterParameter parameter) implements Predicate<T> {

		@Override
		public boolean test(T dto) {
			Object value = getValue(dto, parameter.getName());
			for (String stringValue : parameter.getStringValuesAsList()) {
				if (value != null && Objects.equals(value, TypeConverter.to(value.getClass(), stringValue))) {
					return true;
				}
			}
			return false;
		}

	}

	private record PredicateContainsOneOf<T>(FilterParameter parameter) implements Predicate<T> {

		@Override
		public boolean test(T dto) {
			Object value = getValue(dto, parameter.getName());
			if (value instanceof String str) {
				for (String stringValue : parameter.getStringValuesAsList()) {
					if (StringUtils.containsIgnoreCase(str, stringValue)) {
						return true;
					}
				}
			}
			return false;
		}

	}

}
