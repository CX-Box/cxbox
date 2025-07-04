/*
 * © OOO "SI IKS LAB", 2022-2025
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

package org.cxbox.core.util.filter.drilldowns;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.util.Strings;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.controller.param.SearchOperation;
import org.cxbox.core.dto.multivalue.MultivalueField;
import org.cxbox.core.dto.multivalue.MultivalueFieldSingleValue;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.dictionary.Dictionary;
import org.jetbrains.annotations.NotNull;

public abstract class CxboxDrillDownFilterBuilder<D extends DataResponseDTO, SELF extends FilterBuilder<D, SELF>> implements
		FilterBuilder<D, SELF> {

	private static final String AMPERSAND_URL_ENCODED = URLEncoder.encode("&", StandardCharsets.UTF_8);

	@NonNull
	@Getter
	private final List<DrillDownFieldFilter<D, ?>> fieldFilters = new ArrayList<>();


	/**
	 * Constructs the URL-encoded filter string for the specified business component.
	 * <p>
	 * This method processes all collected field filters and builds a single filter string in the format:
	 * <pre>{@code
	 * filters={"bcName":"filter1&filter2&..."}
	 * }</pre>
	 *
	 * <p>The method performs the following steps:
	 * <ol>
	 *   <li>Returns {@link Optional#empty()} if no filters are present</li>
	 *   <li>Filters out {@code null} and empty field filters</li>
	 *   <li>Returns {@link Optional#empty()} if no valid filters remain after cleaning</li>
	 *   <li>Constructs the filter string by:
	 *     <ul>
	 *       <li>URL-encoding each individual filter</li>
	 *       <li>Joining them with {@code AMPERSAND_URL_ENCODED} (&amp;)</li>
	 *       <li>Wrapping in the required JSON-like structure with the BC name</li>
	 *     </ul>
	 *   </li>
	 * </ol>
	 *
	 * <p>Example output:
	 * <pre>{@code
	 * filters={"MeetingDocument":"priority.equals=1&status.equalsOneOf=[ACTIVE]"}
	 * }</pre>
	 *
	 * @param bc the business component identifier for which to build the filter
	 * @return an {@link Optional} containing the constructed filter string if valid filters exist,
	 * otherwise returns {@link Optional#empty()}
	 */

	protected Optional<String> build(BcIdentifier bc) {
		var cleanedList = fieldFilters.stream()
				.filter(Objects::nonNull)
				.filter(DrillDownFieldFilter::isNotEmpty)
				.toList();
		if (cleanedList.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of("filters={\"" + bc.getName() +
				"\":\"" +
				cleanedList.stream()
						.map(DrillDownFieldFilter::urlEncodedFieldFilter)
						.collect(Collectors.joining(AMPERSAND_URL_ENCODED))
				+ "\"}");
	}




	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "input"} in .widget.json and {@code String} in DTO
	 *
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code CONTAINS} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.input(DTO_.id, "123");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */
	public SELF input(@NonNull DtoField<? super D, String> field, @Nullable String value) {
		final DrillDownFieldFilter<D, String> drillDownFieldFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
		if (drillDownFieldFilter == null) {
			return (SELF) this;
		}
		fieldFilters.add(drillDownFieldFilter);
		return (SELF) this;
	}

	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "dictionary"} in .widget.json and {@code ? extend Dictionary} in DTO
	 *
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code EQUALS_ONE_OF} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.input(DTO_.dictionary, "123");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */
	public <T extends Dictionary> SELF dictionary(@NonNull DtoField<? super D, T> field, @Nullable T value) {
		DrillDownFieldFilter<D, T> drillDownFieldFilter = formDrillDownFieldFilterArraysValue(
				SearchOperation.EQUALS_ONE_OF, true, field, value);
		if (drillDownFieldFilter == null) {
			return (SELF) this;
		}
		this.fieldFilters.add(drillDownFieldFilter);
		return (SELF) this;
	}

	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "dictionary"} in .widget.json and {@code ? extend Enum} in DTO
	 *
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code EQUALS_ONE_OF} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.input(DTO_.dictionaryEnum, Enum.VALUE);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */
	public <T extends Enum<?>> SELF dictionaryEnum(@NonNull DtoField<? super D, T> field, @Nullable T value) {
		DrillDownFieldFilter<D, T> drillDownFieldFilter = formDrillDownFieldFilterArraysValue(
				SearchOperation.EQUALS_ONE_OF, true, field, value);
		if (drillDownFieldFilter == null) {
			return (SELF) this;
		}
		this.fieldFilters.add(drillDownFieldFilter);
		return (SELF) this;
	}

	/**
	 * Creates a {@link DrillDownFieldFilter} that filters records where the specified date/time field
	 * is greater than or equal to the provided value.
	 * <p>
	 * If the {@code value} is {@code null} or the resulting filter cannot be formed,
	 * a {@link DrillDownFieldFilter} with {@code null} as its filter expression is returned.
	 * </p>
	 *
	 * @param field the DTO field to filter by (must not be {@code null})
	 * @param value the lower bound value for the filter (may be {@code null})
	 * @return a {@link DrillDownFieldFilter} for the {@code GREATER_OR_EQUAL_THAN} operation,
	 * or a filter with {@code null} expression if the filter could not be created
	 */
	private DrillDownFieldFilter<D, LocalDateTime> dateFromFilter(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDateTime value) {
		DrillDownFieldFilter<D, LocalDateTime> dateTimeDrillDownFieldFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.GREATER_OR_EQUAL_THAN,
				true,
				field,
				value
		);
		if (dateTimeDrillDownFieldFilter == null) {
			return new DrillDownFieldFilter<>(null);
		}
		return dateTimeDrillDownFieldFilter;
	}


	/**
	 * Creates a {@link DrillDownFieldFilter} that filters records where the specified date/time field
	 * is less than or equal to the provided value.
	 * <p>
	 * If the {@code value} is {@code null} or the resulting filter cannot be formed,
	 * a {@link DrillDownFieldFilter} with {@code null} as its filter expression is returned.
	 * </p>
	 *
	 * @param field the DTO field to filter by (must not be {@code null})
	 * @param value the lower bound value for the filter (may be {@code null})
	 * @return a {@link DrillDownFieldFilter} for the {@code GREATER_OR_EQUAL_THAN} operation,
	 * or a filter with {@code null} expression if the filter could not be created
	 */
	private DrillDownFieldFilter<D, LocalDateTime> dateToFilter(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDateTime value) {
		DrillDownFieldFilter<D, LocalDateTime> dateTimeDrillDownFieldFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.LESS_OR_EQUAL_THAN,
				true,
				field,
				value
		);
		if (dateTimeDrillDownFieldFilter == null) {
			return new DrillDownFieldFilter<>(null);
		}
		return dateTimeDrillDownFieldFilter;
	}

	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "date"} in .widget.json and {@code ? extend LocalDate} in DTO
	 *
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code EQUALS_ONE_OF} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.data(DTO_.createDate, new LocalDateTime().now());
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */
	public SELF date(@NonNull DtoField<? super D, LocalDateTime> field, @Nullable LocalDate value) {
		return this.dateFromTo(field, value, value);
	}

	/**
	 * Attention! Will be moved to {@link DateValueProvider}
	 *
	 * @param field DTO field
	 * @param from date. converted to filter from (including) LocalDateTime by adding 00:00:00
	 * @param to date. converted to filter from (including) LocalDateTime by adding 23:59:59
	 * @return string to concat in drillDown filter
	 */
	/**
	 * Adds a date range filter for the specified field, using the provided start and end dates.
	 * <br> Use only for fields having {@code "type" : "date"} in .widget.json and {@code ? extends LocalDate} in DTO
	 * <p>
	 * This method constructs a filter that matches records where the field's value falls within the specified range:
	 * <ul>
	 *   <li>If both {@code from} and {@code to} are provided, creates a combined filter for the range {@code [from.atStartOfDay(), to.atTime(23:59:59)]}</li>
	 *   <li>If only {@code from} is provided, creates a filter for values {@code >= from.atStartOfDay()}</li>
	 *   <li>If only {@code to} is provided, creates a filter for values {@code <= to.atTime(23:59:59)}</li>
	 *   <li>If both are {@code null}, returns the builder unchanged</li>
	 * </ul>
	 *
	 * <p>Date boundaries are handled as follows:
	 * <ul>
	 *   <li>{@code from} date is converted to {@code LocalDateTime} at start of day (00:00:00)</li>
	 *   <li>{@code to} date is converted to {@code LocalDateTime} at end of day (23:59:59)</li>
	 * </ul>
	 *
	 * Example usage:
	 * <pre>{@code
	 * builder.dateFromTo(Entity_.createdAt,
	 *     LocalDate.of(2023, 1, 1),
	 *     LocalDate.of(2023, 1, 31))
	 * }</pre>
	 *
	 * @param field the date field to filter (must not be {@code null})
	 * @param from the start date (inclusive, may be {@code null})
	 * @param to the end date (inclusive, may be {@code null})
	 * @return this builder instance for fluent chaining
	 */

	@NonNull
	public SELF dateFromTo(@NonNull DtoField<? super D, LocalDateTime> field, @Nullable LocalDate from,
			@Nullable LocalDate to) {
		if (from != null && to != null) {
			this.fieldFilters.add(new DrillDownFieldFilter<>(
					dateFromFilter(field, from.atStartOfDay()).urlEncodedFieldFilter() +
							AMPERSAND_URL_ENCODED +
							dateToFilter(field, to.atTime(23, 59, 59)).urlEncodedFieldFilter()));
			return (SELF) this;
		}
		if (from != null) {
			this.fieldFilters.add(dateFromFilter(field, from.atStartOfDay()));
			return (SELF) this;
		}
		if (to != null) {
			this.fieldFilters.add(dateToFilter(field, to.atTime(23, 59, 59)));
			return (SELF) this;
		}
		return (SELF) this;
	}

	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br> Use only for fields having {@code "type" : "dateTime"} and {@code "type":"dateTimeWithSeconds"}  in .widget.json and {@code ? extends LocalDateTime} in DTO
	 *
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code GREATER_OR_EQUAL_THAN and LESS_OR_EQUAL_THAN} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.data(DTO_.createDate, new LocalDateTime().now());
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */
	@NonNull
	public SELF dateTime(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDateTime value) {
		return this.dateTimeFromTo(field, value, value);
	}

	/**
	 * Adds a date range filter for the specified field, using the provided start and end dates.
	 * <br> Use only for fields having {@code "type" : "dateTime"} and {@code "type":"dateTimeWithSeconds"}  in .widget.json and {@code ? extends LocalDateTime} in DTO
	 * <p>
	 * This method constructs a filter that matches records where the field's value falls within the specified range:
	 * <ul>
	 *   <li>If both {@code from} and {@code to} are provided, creates a combined filter for the range {@code [from, to]}</li>
	 *   <li>If only {@code from} is provided, creates a filter for values {@code >= from}</li>
	 *   <li>If only {@code to} is provided, creates a filter for values {@code <= to}</li>
	 *   <li>If both are {@code null}, returns the builder unchanged</li>
	 * </ul>
	 *
	 * Example usage:
	 * <pre>{@code
	 * builder.dateFromTo(Entity_.createdAt,
	 *     LocalDate.now().minusYear(1),
	 *     LocalDate.now().plusYear(1))
	 * }</pre>
	 *
	 * @param field the date field to filter (must not be {@code null})
	 * @param from the start date (inclusive, may be {@code null})
	 * @param to the end date (inclusive, may be {@code null})
	 * @return this builder instance for fluent chaining
	 */
	@NonNull
	public SELF dateTimeFromTo(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDateTime from,
			@Nullable LocalDateTime to) {
		if (from != null && to != null) {
			this.fieldFilters.add(new DrillDownFieldFilter<>(
					dateFromFilter(field, from).urlEncodedFieldFilter() +
							AMPERSAND_URL_ENCODED +
							dateToFilter(field, to).urlEncodedFieldFilter()));
			return (SELF) this;
		}
		if (from != null) {
			this.fieldFilters.add(dateFromFilter(field, from));
			return (SELF) this;
		}
		if (to != null) {
			this.fieldFilters.add(dateToFilter(field, to));
			return (SELF) this;
		}
		return (SELF) this;
	}


	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "multivalue"} in .widget.json and {@code  MultivalueField} in DTO
	 *
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code EQUALS_ONE_OF} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.input(DTO_.id, "123");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */
	public SELF multiValue(@NonNull DtoField<? super D, MultivalueField> field,
			@Nullable MultivalueField value) {

		DrillDownFieldFilter<D, MultivalueField> drillDownFieldFilter = formDrillDownFieldFilterMultivalueFieldValue(
				SearchOperation.EQUALS_ONE_OF,
				field,
				value,
				MultivalueFieldSingleValue::getId
		);
		if (drillDownFieldFilter == null) {
			return (SELF) this;
		}
		this.fieldFilters.add(drillDownFieldFilter);
		return (SELF) this;
	}

	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "number"} in .widget.json and {@code  ? extend Number} in DTO
	 * <br>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code EQUALS_ONE_OF} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.number(DTO_.id, 123L);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */
	@NonNull
	public <T extends Number> SELF number(@NonNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return (SELF) this;
		}

		DrillDownFieldFilter<D, T> numberDrillDownFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.EQUALS,
				field,
				value
		);
		if (numberDrillDownFilter != null) {
			this.fieldFilters.add(numberDrillDownFilter);
		}
		return (SELF) this;
	}

	/**
	 * Adds a date range filter for the specified field, using the provided start and end dates.
	 * <br> Use only for fields having {@code "type" : "number"} in .widget.json and {@code ? extends Number} in DTO
	 * <br>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * <p>
	 * This method constructs a filter that matches records where the field's value falls within the specified range:
	 * <ul>
	 *   <li>If both {@code from} and {@code to} are provided, creates a combined filter for the range {@code [from, to]}</li>
	 *   <li>If only {@code from} is provided, creates a filter for values {@code >= from}</li>
	 *   <li>If only {@code to} is provided, creates a filter for values {@code <= to}</li>
	 *   <li>If both are {@code null}, returns the builder unchanged</li>
	 * </ul>
	 *
	 * Example usage:
	 * <pre>{@code
	 * ...
	 * builder.dateFromTo(Entity_.value, 10L, 20L)
	 * ...
	 * }</pre>
	 *
	 * @param field the date field to filter (must not be {@code null})
	 * @param from the start date (inclusive, may be {@code null})
	 * @param to the end date (inclusive, may be {@code null})
	 * @return this builder instance for fluent chaining
	 */
	@NonNull
	public <T extends Number> SELF numberFromTo(@NonNull DtoField<? super D, T> field, @Nullable T from,
			@Nullable T to) {
		if (from == null && to == null) {
			return (SELF) this;
		}

		if (from != null && to == null) {
			this.fieldFilters.add(numberFrom(field, from));
		} else if (from == null && to != null) {
			this.fieldFilters.add(
					numberTo(field, to)
			);
		} else {
			this.fieldFilters.add(new DrillDownFieldFilter<>(
					this.numberFrom(field, from).urlEncodedFieldFilter()
							+ AMPERSAND_URL_ENCODED
							+ this.numberTo(field, to).urlEncodedFieldFilter()
			));
		}

		return (SELF) this;
	}

	/**
	 * Creates a {@link DrillDownFieldFilter} that filters records where the specified  number field
	 * is greater than or equal to the provided value.
	 * <br> Use only for fields having {@code "type" : "number"} in .widget.json and {@code ? extends Number} in DTO
	 * <p>
	 * If the {@code value} is {@code null} or the resulting filter cannot be formed,
	 * a {@link DrillDownFieldFilter} with {@code null} as its filter expression is returned.
	 * </p>
	 *
	 * @param field the DTO field to filter by (must not be {@code null})
	 * @param value the lower bound value for the filter (may be {@code null})
	 * @return a {@link DrillDownFieldFilter} for the {@code GREATER_OR_EQUAL_THAN} operation,
	 * or a filter with {@code null} expression if the filter could not be created
	 */
	private <T extends Number> DrillDownFieldFilter<D, T> numberFrom(@NonNull DtoField<? super D, T> field,
			@Nullable T value) {
		DrillDownFieldFilter<D, T> drillDownFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.GREATER_OR_EQUAL_THAN,
				field,
				value
		);
		if (drillDownFilter == null) {
			return new DrillDownFieldFilter<>(null);
		}
		return drillDownFilter;
	}

	/**
	 * Creates a {@link DrillDownFieldFilter} that filters records where the specified  number field
	 * is less than or equal to the provided value.
	 * <br> Use only for fields having {@code "type" : "number"} in .widget.json and {@code ? extends Number} in DTO
	 * <p>
	 * If the {@code value} is {@code null} or the resulting filter cannot be formed,
	 * a {@link DrillDownFieldFilter} with {@code null} as its filter expression is returned.
	 * </p>
	 *
	 * @param field the DTO field to filter by (must not be {@code null})
	 * @param value the lower bound value for the filter (may be {@code null})
	 * @return a {@link DrillDownFieldFilter} for the {@code LESS_OR_EQUAL_THAN} operation,
	 * or a filter with {@code null} expression if the filter could not be created
	 */
	private <T extends Number> DrillDownFieldFilter<D, T> numberTo(@NonNull DtoField<? super D, T> field,
			@Nullable T value) {
		DrillDownFieldFilter<D, T> drillDownFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.LESS_OR_EQUAL_THAN,
				field,
				value
		);
		if (drillDownFilter == null) {
			return new DrillDownFieldFilter<>(null);
		}
		return drillDownFilter;
	}


	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "percent"} in .widget.json and {@code  ? extend Number} in DTO
	 * <br>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code EQUALS_ONE_OF} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.percent(DTO_.id, 123L);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */
	@NonNull
	public <T extends Number> SELF percent(@NonNull DtoField<? super D, T> field, T value) {
		return number(field, value);
	}

	/**
	 * Adds a date range filter for the specified field, using the provided start and end dates.
	 * <br> Use only for fields having {@code "type" : "percent"} in .widget.json and {@code ? extends Number} in DTO
	 * <br>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * <p>
	 * This method constructs a filter that matches records where the field's value falls within the specified range:
	 * <ul>
	 *   <li>If both {@code from} and {@code to} are provided, creates a combined filter for the range {@code [from, to]}</li>
	 *   <li>If only {@code from} is provided, creates a filter for values {@code >= from}</li>
	 *   <li>If only {@code to} is provided, creates a filter for values {@code <= to}</li>
	 *   <li>If both are {@code null}, returns the builder unchanged</li>
	 * </ul>
	 *
	 * Example usage:
	 * <pre>{@code
	 * ...
	 * builder.dateFromTo(Entity_.value, 10L, 20L)
	 * ...
	 * }</pre>
	 *
	 * @param field the date field to filter (must not be {@code null})
	 * @param from the start date (inclusive, may be {@code null})
	 * @param to the end date (inclusive, may be {@code null})
	 * @return this builder instance for fluent chaining
	 */
	@NonNull
	public <T extends Number> SELF percentFromTo(@NonNull DtoField<? super D, T> field, T from, T to) {
		return numberFromTo(field, from, to);
	}


	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "text"} in .widget.json and {@code String} in DTO
	 *
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code CONTAINS} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.text(DTO_.description, "123");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */
	public SELF text(@NonNull DtoField<? super D, String> field, String value) {
		return input(field, value);
	}

	/**
	 * Adds a filter that matches records where the specified enum field is equal to one of the provided values.
	 * <p>
	 * This method is useful for filtering by a set of possible enum values
	 * If the provided {@code values} array is empty or the filter cannot be created, the builder remains unchanged.
	 * </p>
	 *
	 * <pre>{@code
	 * // Example: filter by status being either ACTIVE or INACTIVE
	 * builder.radio(Entity_.status, Status.ACTIVE, Status.INACTIVE);
	 * }</pre>
	 *
	 * @param <T> the enum type of the field
	 * @param field the DTO enum field to filter by (must not be {@code null})
	 * @param values one or more enum values to match (may be empty)
	 * @return this builder instance for fluent chaining
	 */
	public <T extends Enum<?>> SELF radio(@NotNull DtoField<? super D, T> field, T... values) {
		DrillDownFieldFilter<D, T> drillDownFieldFilter = formDrillDownFieldFilterArraysValue(
				SearchOperation.EQUALS_ONE_OF,
				true,
				field,
				values
		);
		if (drillDownFieldFilter == null) {
			return (SELF) this;
		}
		this.fieldFilters.add(drillDownFieldFilter);
		return (SELF) this;
	}

	/**
	 * Adds a filter to match records where the specified boolean field is set to the given value.
	 * <p>
	 * This method is typically used for filtering by checkbox UI controls, allowing you to filter
	 * records based on whether a boolean field is {@code true} or {@code false}.
	 * If the filter cannot be created, the builder remains unchanged.
	 * </p>
	 *
	 * <pre>{@code
	 * // Example: filter by 'active' field being true
	 * builder.checkbox(Entity_.active, true);
	 * }</pre>
	 *
	 * @param field the DTO boolean field to filter by (must not be {@code null})
	 * @param value the boolean value to match (true or false)
	 * @return this builder instance for fluent chaining
	 */
	public SELF checkbox(@NotNull DtoField<? super D, Boolean> field, boolean value) {
		DrillDownFieldFilter<D, Boolean> drillDownFieldFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.SPECIFIED,
				field,
				value
		);
		if (drillDownFieldFilter == null) {
			return (SELF) this;
		}
		this.fieldFilters.add(drillDownFieldFilter);
		return (SELF) this;
	}

	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "money"} in .widget.json and {@code  ? extend Number} in DTO
	 * <br>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code EQUALS_ONE_OF} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.money(DTO_.id, 123L);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */
	public <T extends Number> SELF money(@NotNull DtoField<? super D, T> field, T value) {
		return number(field, value);
	}

	/**
	 * Adds a date range filter for the specified field, using the provided start and end dates.
	 * <br> Use only for fields having {@code "type" : "money"} in .widget.json and {@code ? extends Number} in DTO
	 * <br>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * <p>
	 * This method constructs a filter that matches records where the field's value falls within the specified range:
	 * <ul>
	 *   <li>If both {@code from} and {@code to} are provided, creates a combined filter for the range {@code [from, to]}</li>
	 *   <li>If only {@code from} is provided, creates a filter for values {@code >= from}</li>
	 *   <li>If only {@code to} is provided, creates a filter for values {@code <= to}</li>
	 *   <li>If both are {@code null}, returns the builder unchanged</li>
	 * </ul>
	 *
	 * Example usage:
	 * <pre>{@code
	 * ...
	 * builder.moneyFromTo(Entity_.value, 10L, 20L)
	 * ...
	 * }</pre>
	 *
	 * @param field the date field to filter (must not be {@code null})
	 * @param from the start date (inclusive, may be {@code null})
	 * @param to the end date (inclusive, may be {@code null})
	 * @return this builder instance for fluent chaining
	 */
	public <T extends Number> SELF moneyFromTo(@NotNull DtoField<? super D, T> field, T from, T to) {
		return numberFromTo(field, from, to);
	}


	/**
	 * Adds a filter that matches records where the specified field contains the given value.
	 * <p>
	 * This method is typically used for filtering by pick list or dropdown controls in the UI,
	 * allowing you to filter records based on whether the field contains the selected value.
	 * If the filter cannot be created (for example, if the value is {@code null} or invalid),
	 * the builder remains unchanged.
	 * </p>
	 *
	 * <pre>{@code
	 * // Example:
	 * builder.fileUpload(Employee_.fileID, "UUID");
	 * }</pre>
	 *
	 * @param <T> the type of the field value, which must be {@link Serializable}
	 * @param field the DTO field to filter by (must not be {@code null})
	 * @param value the value to search for in the field (may be {@code null})
	 * @return this builder instance for fluent chaining
	 */
	public <T extends Serializable> SELF fileUpload(@NotNull DtoField<? super D, T> field, T value) {
		DrillDownFieldFilter<D, T> dtDrillDownFieldFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
		if (dtDrillDownFieldFilter != null) {
			this.fieldFilters.add(
					dtDrillDownFieldFilter
			);
		}
		return (SELF) this;
	}

	/**
	 * Adds a filter that matches records where the specified field contains the given value.
	 * <p>
	 * This method is typically used for filtering by pick list or dropdown controls in the UI,
	 * allowing you to filter records based on whether the field contains the selected value.
	 * If the filter cannot be created (for example, if the value is {@code null} or invalid),
	 * the builder remains unchanged.
	 * </p>
	 *
	 * <pre>{@code
	 * // Example: filter by department containing "IT"
	 * builder.inlinePickList(Employee_.department, "IT");
	 * }</pre>
	 *
	 * @param <T> the type of the field value, which must be {@link Serializable}
	 * @param field the DTO field to filter by (must not be {@code null})
	 * @param value the value to search for in the field (may be {@code null})
	 * @return this builder instance for fluent chaining
	 */
	public <T extends Serializable> SELF pickList(@NotNull DtoField<? super D, T> field, T value) {
		DrillDownFieldFilter<D, T> dtDrillDownFieldFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
		if (dtDrillDownFieldFilter != null) {
			this.fieldFilters.add(
					dtDrillDownFieldFilter
			);
		}
		return (SELF) this;
	}


	/**
	 * Adds a filter that matches records where the specified field contains the given value.
	 * <p>
	 * This method is typically used for filtering by pick list or dropdown controls in the UI,
	 * allowing you to filter records based on whether the field contains the selected value.
	 * If the filter cannot be created (for example, if the value is {@code null} or invalid),
	 * the builder remains unchanged.
	 * </p>
	 *
	 * <pre>{@code
	 * // Example: filter by department containing "IT"
	 * builder.inlinePickList(Employee_.department, "IT");
	 * }</pre>
	 *
	 * @param <T> the type of the field value, which must be {@link Serializable}
	 * @param field the DTO field to filter by (must not be {@code null})
	 * @param value the value to search for in the field (may be {@code null})
	 * @return this builder instance for fluent chaining
	 */
	public <T extends Serializable> SELF inlinePickList(@NotNull DtoField<? super D, T> field, T value) {
		DrillDownFieldFilter<D, T> dtDrillDownFieldFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
		if (dtDrillDownFieldFilter != null) {
			this.fieldFilters.add(
					dtDrillDownFieldFilter
			);
		}
		return (SELF) this;
	}

	/**
	 * Adds a filter that matches records where the specified field contains the given value.
	 * <p>
	 * This method is typically used for filtering by pick list or dropdown controls in the UI,
	 * allowing you to filter records based on whether the field contains the selected value.
	 * If the filter cannot be created (for example, if the value is {@code null} or invalid),
	 * the builder remains unchanged.
	 * </p>
	 *
	 * <pre>{@code
	 * // Example: filter by department containing "IT"
	 * builder.inlinePickList(Employee_.department, "IT");
	 * }</pre>
	 *
	 * @param <T> the type of the field value, which must be {@link Serializable}
	 * @param field the DTO field to filter by (must not be {@code null})
	 * @param value the value to search for in the field (may be {@code null})
	 * @return this builder instance for fluent chaining
	 */
	public <T extends Serializable> SELF multifield(@NotNull DtoField<? super D, T> field, T value) {
		DrillDownFieldFilter<D, T> dtDrillDownFieldFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
		if (dtDrillDownFieldFilter != null) {
			this.fieldFilters.add(
					dtDrillDownFieldFilter
			);
		}
		return (SELF) this;
	}

	/**
	 * Adds a filter that matches records where the specified field contains the given value.
	 * <p>
	 * This method is typically used for filtering by pick list or dropdown controls in the UI,
	 * allowing you to filter records based on whether the field contains the selected value.
	 * If the filter cannot be created (for example, if the value is {@code null} or invalid),
	 * the builder remains unchanged.
	 * </p>
	 *
	 * <pre>{@code
	 * // Example: filter by department containing "IT"
	 * builder.inlinePickList(Employee_.department, "IT");
	 * }</pre>
	 *
	 * @param <T> the type of the field value, which must be {@link Serializable}
	 * @param field the DTO field to filter by (must not be {@code null})
	 * @param value the value to search for in the field (may be {@code null})
	 * @return this builder instance for fluent chaining
	 */
	public <T extends Serializable> SELF suggestionPickList(@NotNull DtoField<? super D, T> field, T value) {
		DrillDownFieldFilter<D, T> dtDrillDownFieldFilter = formDrillDownFieldFilterSingleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
		if (dtDrillDownFieldFilter != null) {
			this.fieldFilters.add(
					dtDrillDownFieldFilter
			);
		}
		return (SELF) this;
	}

	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "multivalueHover"} in .widget.json and {@code  MultivalueField} in DTO
	 *
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code EQUALS_ONE_OF} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.multivalueHover(DTO_.id, "123");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */

	public SELF multivalueHover(@NotNull DtoField<? super D, MultivalueField> field, MultivalueField value) {
		return multiValue(field, value);
	}


	/**
	 * Adds a filter to the builder that checks whether the given field contains the specified value.
	 * <br>Use only for fields having {@code "type" : "multipleSelect"} in .widget.json and {@code  MultivalueField} in DTO
	 *
	 * <p>
	 * If the value is {@code null} or the resulting filter is {@code null}, this method does nothing and returns the builder unchanged.
	 * Otherwise, it adds a {@link DrillDownFieldFilter} with the {@code EQUALS_ONE_OF} operation for the specified field and value.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * FilterBuilder<DTO, FilterBuilderDefault> fb = new FilterBuilderDefault<>();
	 * // ...
	 * fb.multipleSelect(DTO_.id, "123");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return this builder {@code ? extend FilterBuilder} instance for fluent chaining
	 */

	public <T extends MultivalueField> SELF multipleSelect(@NotNull DtoField<? super D, T> field, T value) {
		DrillDownFieldFilter<D, T> drillDownFieldFilter = formDrillDownFieldFilterMultivalueFieldValue(
				SearchOperation.EQUALS_ONE_OF,
				field,
				value,
				MultivalueFieldSingleValue::getValue
		);
		if (drillDownFieldFilter == null) {
			return (SELF) this;
		}
		this.fieldFilters.add(drillDownFieldFilter);
		return (SELF) this;
	}



	/*


	 */


	/*


	 */


	/**
	 * Creates a {@link DrillDownFieldFilter} that filters records where the specified  MultivalueField field
	 * with search operation
	 *
	 * <p>
	 * If the {@code value} is {@code null} or the resulting filter cannot be formed,
	 * a {@link DrillDownFieldFilter} with {@code null} as its filter expression is returned.
	 * </p>
	 *
	 * <pre>
	 *   {@code
	 *      Example: // filtration with search operation EQUALS_ONE_OF and function data extractor by id
	 *    		DrillDownFieldFilter<D, MultivalueField> drillDownFieldFilter = formDrillDownFieldFilterMultivalueFieldValue(
	 * 				SearchOperation.EQUALS_ONE_OF,
	 * 				field,
	 * 				value,
	 * 				MultivalueFieldSingleValue::getId
	 * 		);
	 *    }
	 *    </pre>
	 * </p>
	 *
	 * @param searchOperation the type of search operation
	 * @param field the DTO field to filter by
	 * @param value value for filter
	 * @param functionData - function to extract data from MultivalueFieldSingleValue
	 * @param <T> -{@code <T extends MultivalueField>}
	 * @return {@code DrillDownFieldFilter} if {@code value!=null}, else {@code null}
	 */
	protected <T extends MultivalueField> DrillDownFieldFilter<D, T> formDrillDownFieldFilterMultivalueFieldValue(
			@NotNull SearchOperation searchOperation, @NotNull DtoField<? super D, T> field, T value,
			Function<MultivalueFieldSingleValue, String> functionData) {
		if (value == null) {
			return null;
		}
		return new DrillDownFieldFilter<>(URLEncoder.encode(
				field.getName() +
						"." + searchOperation.getOperationName() +
						"=" + "[\\\"" + Optional.of(value)
						.map(mv -> mv.getValues().stream()
								.map(functionData)
								.map(URLEncoder::encode)
								.collect(Collectors.joining("\\\",\\\"")))
						.orElseThrow() + "\\\"]",
				StandardCharsets.UTF_8
		));
	}


	protected <T> DrillDownFieldFilter<D, T> formDrillDownFieldFilterSingleValue(@NotNull SearchOperation operation,
			@NotNull DtoField<? super D, T> field,
			@Nullable T value) {
		return formDrillDownFieldFilterSingleValue(operation, false, field, value);
	}

	protected <T> DrillDownFieldFilter<D, T> formDrillDownFieldFilterSingleValue(@NotNull SearchOperation operation,
			boolean isJson, @NotNull DtoField<? super D, T> field,
			@Nullable T value) {
		if (value == null) {
			return null;
		}
		String val;
		if (isJson) {
			val = JsonUtils.writeValue(value).substring(1, JsonUtils.writeValue(value).length() - 1);
		} else {
			val = String.valueOf(value);
		}
		return new DrillDownFieldFilter<>(
				URLEncoder.encode(
						field.getName() + "." + operation.getOperationName() + "=" + val,
						StandardCharsets.UTF_8
				)
		);
	}

	protected <T> DrillDownFieldFilter<D, T> formDrillDownFieldFilterArraysValue(SearchOperation operation,
			@NotNull DtoField<? super D, T> field,
			T... values) {
		return formDrillDownFieldFilterArraysValue(operation, false, field, values);
	}

	protected <T> DrillDownFieldFilter<D, T> formDrillDownFieldFilterArraysValue(SearchOperation operation,
			boolean isJson, @NotNull DtoField<? super D, T> field,
			T... values) {
		if (values == null || values.length < 1 || Arrays.stream(values).noneMatch(Objects::nonNull)) {
			return null;
		}
		return new DrillDownFieldFilter<>(
				URLEncoder.encode(
						field.getName() +
								"." + operation.getOperationName() +
								"=" + "[\\\"" +
								Arrays.stream(values)
										.map(val -> isJson ? JsonUtils.writeValue(val) : String.valueOf(val))
										.map(s -> isJson ? (s.substring(1, s.length() - 1)) : s)
										.map(URLEncoder::encode)
										.collect(Collectors.joining(","))
								+ "\\\"]",
						StandardCharsets.UTF_8
				)
		);
	}

	protected <T> SELF add(DrillDownFieldFilter<D, T> drillDownFieldFilter) {
		this.fieldFilters.add(drillDownFieldFilter);
		return (SELF) this;
	}

	public record DrillDownFieldFilter<D, T>(String urlEncodedFieldFilter) {

		public boolean isNotEmpty() {
			return !Strings.isBlank(urlEncodedFieldFilter);
		}

	}

}
