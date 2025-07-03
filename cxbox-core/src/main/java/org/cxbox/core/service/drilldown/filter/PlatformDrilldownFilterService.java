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

package org.cxbox.core.service.drilldown.filter;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.controller.param.SearchOperation;
import org.cxbox.core.dto.multivalue.MultivalueField;
import org.cxbox.core.dto.multivalue.MultivalueFieldSingleValue;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.dictionary.Dictionary;


/**
 * Service for building URL-encoded filter expressions for platform business components.
 * <p>
 * This service provides methods to create filter strings compatible with various widget types
 * and search operations. Each method handles null values gracefully and returns URL-encoded
 * filter expressions ready for use in platform requests.
 * </p>

 *<p>
 * Example Usage
 * <pre>{@code
 * PlatformUrlParametersFilterService service = new PlatformUrlParametersFilterService();
 *
 * // Single field filters
 * String nameFilter = service.input(MyDTO_.name, "searchText");
 * String statusFilter = service.dictionary(MyDTO_.status, Status.ACTIVE);
 * String priceFilter = service.money(MyDTO_.price, 100L);
 *
 * // Range filters
 * String dateRangeFilter = service.dateFromTo(MyDTO_.createdAt,
 *     LocalDate.of(2025, 1, 1),
 *     LocalDate.of(2025, 12, 31));
 *
 * }</pre>
 *</p>
 */
public class PlatformDrilldownFilterService {

	private static final String AMPERSAND_URL_ENCODED = URLEncoder.encode("&", StandardCharsets.UTF_8);


	// TODO проверить что формируется и поправить пример
	/**
	 * Constructs a JSON-style URL-encoded filter fragment for the specified business component.

	 * <p>This method processes the provided collection of filter expressions and builds
	 * a single JSON-like fragment in the format:
	 * <pre>{@code
	 * "<bcName>":"<filter1>&<filter2>&…"
	 * }</pre>
	 * where each element has already been URL-encoded and individual filters
	 * are joined by the pre-encoded ampersand constant {@code AMPERSAND_URL_ENCODED}.
	 *
	 * <p>Processing steps:
	 * <ol>
	 *   <li>Remove any {@code null} or empty strings from {@code fieldsFilterString}.</li>
	 *   <li>If no valid filters remain, return {@link Optional#empty()}.</li>
	 *   <li>Otherwise, join the cleaned filters with {@code AMPERSAND_URL_ENCODED}, wrap the result
	 *       in quotes and prepend the BC name in quotes, producing a single fragment.</li>
	 * </ol>
	 *
	 * <p>Example:
	 * <pre>{@code
	 * Collection<String> filters = List.of("field1.equals=value1", "field2.equals=value2");
	 * Optional<String> part = formUrlPart(myBc, filters);
	 * // part -> Optional.of("\"myBc\":\"field1.equals=value1%26field2.in=[value2]\"")
	 * }</pre>
	 *
	 * @param bc                  the business component identifier {@link BcIdentifier} (must not be {@code null})
	 * @param fieldsFilterString  a collection of filter strings; {@code null} or empty elements
	 *                            will be ignored
	 * @return an {@link Optional} containing the formatted filter fragment if at least one
	 *         non-empty filter is present; otherwise, {@link Optional#empty()}
	 */
	public Optional<String> formUrlPart(BcIdentifier bc, Collection<String> fieldsFilterString) {
		var cleanedList = fieldsFilterString.stream()
				.filter(Objects::nonNull)
				.filter(StringUtils::isNotEmpty)
				.toList();
		if (cleanedList.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of("\"" + bc.getName() +
				"\":\"" +
				String.join(AMPERSAND_URL_ENCODED, cleanedList)
				+ "\"");
	}

	/**
	 * Generates url encoded string a CONTAINS filter for a single string value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "input"} in *.widget.json and {@code String} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.input(MyDTO_.name, "searchText");
	 * }</pre>
	 * </p>
	 * @param field DTO field (non-null)
	 * @param value substring to match (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO> String input(@NonNull DtoField<? super D, String> field, @Nullable String value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
	}

	/**
	 * Generates url encoded string a EQUALS_ONE_OF filters for single dictionary value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dictionary"} in *.widget.json and {@code ? implements Dictionary} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.input(MyDTO_.dictionary, MyDictionary.VALUE1);
	 * }</pre>
	 * </p>
	 * @param field DTO field (non-null)
	 * @param value substring to match (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Dictionary> String dictionary(@NonNull DtoField<? super D, T> field,
			@Nullable T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.arrayValue(
				SearchOperation.EQUALS_ONE_OF,
				true,
				field,
				List.of(value)
		);

	}



	/**
	 * Generates url encoded string a EQUALS_ONE_OF filters for a collection dictionary value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dictionary"} in *.widget.json and {@code ? implements Dictionary} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.dictionary(MyDTO_.dictionary, List.of(MyDictionary.VALUE1, MyDictionary.VALUE2));
	 * }</pre>
	 * </p>
	 * @param field DTO field (non-null)
	 * @param values the collection value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Dictionary> String dictionary(@NonNull DtoField<? super D, T> field,
			@Nullable Collection<T> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.arrayValue(SearchOperation.EQUALS_ONE_OF, true, field, values)
				;
	}

	/**
	 * Generates url encoded string a EQUALS_ONE_OF filter for a single enum value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dictionary"} in *.widget.json and {@code ? extends Enum<?>} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.dictionary(MyDTO_.dictionaryEnum, MyDictionaryEnum.VALUE1);
	 * }</pre>
	 * </p>
	 * @param field DTO field (non-null)
	 * @param value a single enum value. substring to match (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Enum<?>> String dictionaryEnum(@NonNull DtoField<? super D, T> field,
			@Nullable T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.arrayValue(
				SearchOperation.EQUALS_ONE_OF,
				true,
				field,
				List.of(value)
		);
	}


	/**
	 * Generates url encoded string a EQUALS_ONE_OF filter for a collection enum value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dictionary"} in *.widget.json and {@code ? extends Enum<?>} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.dictionaryEnum(MyDTO_.dictionary, List.of(MyDictionary.VALUE1,MyDictionary.VALUE2));
	 * }</pre>
	 * </p>
	 * @param field DTO field (non-null)
	 * @param values a collection enum value (nullable )
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Enum<?>> String dictionaryEnum(@NonNull DtoField<? super D, T> field,
			@Nullable Collection<T> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.arrayValue(SearchOperation.EQUALS_ONE_OF, true, field, values);
	}

	private <D extends DataResponseDTO> String dateFromFilter(@NonNull DtoField<? super D, LocalDateTime> field,
			@NonNull LocalDateTime value) {
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.GREATER_OR_EQUAL_THAN,
				true,
				field,
				value
		);
	}

	private <D extends DataResponseDTO> String dateToFilter(@NonNull DtoField<? super D, LocalDateTime> field,
			@NonNull LocalDateTime value) {
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.LESS_OR_EQUAL_THAN,
				true,
				field,
				value
		);
	}

	/**
	 * Generates url encoded string with GREATER_OR_EQUAL_THAN and LESS_OR_EQUAL_THAN filter for a single date value
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "date"} in *.widget.json and {@code LocalDateTime} type in DTO.
	 * <br><b>Note:</b>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * </p>
	 * <p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.date(MyDTO_.date, LocalDate.now());
	 * }</pre>
	 * </p>
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO> String date(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDate value) {
		if (value == null) {
			return null;
		}
		return this.dateFromTo(field, value, value);
	}

	/**
	 * Generates url encoded string with GREATER_OR_EQUAL_THAN and LESS_OR_EQUAL_THAN filter for a range date
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "date"} in *.widget.json and {@code LocalDateTime} type in DTO.
	 * <br><b>Note:</b>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * </p>
	 * <p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.dateFromTo(MyDTO_.date,
	 *     LocalDate.of(2025, 1, 1),
	 *     LocalDate.of(2025, 1, 31))
	 * }</pre>
	 * @param field the  field to filter (must not be {@code null})
	 * @param from the lower bound  (inclusive, may be {@code null})
	 * @param to the higher bound date (inclusive, may be {@code null})
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO> String dateFromTo(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDate from,
			@Nullable LocalDate to) {
		if (from != null && to != null) {
			return dateFromFilter(field, from.atStartOfDay()) + AMPERSAND_URL_ENCODED + dateToFilter(field, to.atTime(23, 59, 59));
		}
		if (from != null) {
			return dateFromFilter(field, from.atStartOfDay());
		}
		if (to != null) {
			return dateToFilter(field, to.atTime(23, 59, 59));
		}
		return null;
	}

	/**
	 * Generates url encoded string with GREATER_OR_EQUAL_THAN and LESS_OR_EQUAL_THAN filter for a range datetime
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dateTime"} in *.widget.json and {@code LocalDateTime} type in DTO.
	 * <br><b>Note:</b>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.dictionary(MyDTO_.dateTime, LocalDateTime.now());
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO> String dateTime(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDateTime value) {
		if (value == null) {
			return null;
		}
		return this.dateTimeFromTo(field, value, value);
	}

	/**
	 * Generates url encoded string with GREATER_OR_EQUAL_THAN and LESS_OR_EQUAL_THAN filter for a range date
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dateTime"} in *.widget.json and {@code LocalDateTime} type in DTO.
	 * <br><b>Note:</b>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.dateFromTo(MyDTO_.dateTime,
	 *     LocalDateTime.now(),
	 *     LocalDateTime.now().plusYear(1))
	 * }</pre>
	 * @param field the  field to filter (must not be {@code null})
	 * @param from the lower bound  (inclusive, may be {@code null})
	 * @param to the higher bound date (inclusive, may be {@code null})
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO> String dateTimeFromTo(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDateTime from,
			@Nullable LocalDateTime to) {
		if (from != null && to != null) {
			return
					dateFromFilter(field, from) +
							AMPERSAND_URL_ENCODED +
							dateToFilter(field, to);
		}
		if (from != null) {
			return dateFromFilter(field, from);
		}
		if (to != null) {
			return dateToFilter(field, to);
		}
		return null;
	}

	/**
	 * Generates url encoded string with EQUALS_ONE_OF filter for a value
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "multivalue"} in *.widget.json and {@code MultivalueField} type in DTO.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.multivalue(MyDTO_.name, myMultivalue);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO> String multiValue(@NonNull DtoField<? super D, MultivalueField> field,
			@Nullable MultivalueField value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.multivalue(
				SearchOperation.EQUALS_ONE_OF,
				field,
				value,
				MultivalueFieldSingleValue::getId
		);

	}

	/**
	 * Generates url encoded string with EQUALS filter for a value
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "number"} in *.widget.json and {@code ? extends Number} type in DTO.
	 * <br><b>Note:</b>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.number(MyDTO_.id, 111111111L);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Number> String number(@NonNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.EQUALS,
				field,
				value
		);
	}

	/**
	 * Generates url encoded string with GREATER_OR_EQUAL_THAN and LESS_OR_EQUAL_THAN filter for a range value
	 * <p>
	 * <b>Note:</b>Use only for fields having {@code "type" : "number"} in .widget.json and {@code  ? extend Number} in DTO
	 * <br><b>Note:</b>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.number(MyDTO_.id, 111111111L);
	 * }</pre>
	 *</p>
	 *
	 * @param field the date field to filter (must not be {@code null})
	 * @param from the lower bound(inclusive, may be {@code null})
	 * @param to the upper bound (inclusive, may be {@code null})
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Number> String numberFromTo(@NonNull DtoField<? super D, T> field,
			@Nullable T from,
			@Nullable T to) {
		if (from == null && to == null) {
			return null;
		}
		if (from != null && to == null) {
			return numberFrom(field, from);
		} else if (from == null) {
			return numberTo(field, to);
		} else {
			return this.numberFrom(field, from)
							+ AMPERSAND_URL_ENCODED
							+ this.numberTo(field, to);
		}
	}

	private <D extends DataResponseDTO, T extends Number> String numberFrom(@NonNull DtoField<? super D, T> field,
			@NonNull T value) {
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.GREATER_OR_EQUAL_THAN,
				field,
				value
		);
	}

	private <D extends DataResponseDTO, T extends Number> String numberTo(@NonNull DtoField<? super D, T> field,
			@NonNull T value) {
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.LESS_OR_EQUAL_THAN,
				field,
				value
		);
	}

	/**
	 * Generates url encoded string with EQUALS filter for a value
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "percent"} in *.widget.json and {@code ? extends Number} type in DTO.
	 * <br><b>Note:</b>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.percent(MyDTO_.percent, 10);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Number> String percent(@NonNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return null;
		}
		return
				DrillDownFieldFilterFormerUtils.singleValue(
						SearchOperation.EQUALS,
						field,
						value
				);
	}

	/**
	 * Generates url encoded string with GREATER_OR_EQUAL_THAN and LESS_OR_EQUAL_THAN filter for a range value
	 * <p>
	 * <b>Note:</b>Use only for fields having {@code "type" : "percent"} in .widget.json and {@code  ? extend Number} in DTO
	 * <br><b>Note:</b>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.percent(MyDTO_.percent, 1, 10);
	 * }</pre>
	 *</p>
	 *
	 * @param field the date field to filter (must not be {@code null})
	 * @param from the lower bound(inclusive, may be {@code null})
	 * @param to the upper bound (inclusive, may be {@code null})
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Number> String percentFromTo(@NonNull DtoField<? super D, T> field,
			T from, T to) {
		if (from == null && to == null) {
			return null;
		}
		if (from != null && to == null) {
			return numberFrom(field, from);
		} else if (from == null) {
			return
					numberTo(field, to);
		} else {
			return
					this.numberFrom(field, from)
							+ AMPERSAND_URL_ENCODED
							+ this.numberTo(field, to);
		}
	}

	/**
	 * Generates url encoded string a CONTAINS filter for a single string value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "text"} in *.widget.json and {@code String} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.text(MyDTO_.text, "searchText");
	 * }</pre>
	 * </p>
	 * @param field DTO field (non-null)
	 * @param value substring to match (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO> String text(@NonNull DtoField<? super D, String> field, String value) {
		if (value == null) {
			return null;
		}
		return
				DrillDownFieldFilterFormerUtils.singleValue(
						SearchOperation.CONTAINS,
						field,
						value
				);
	}

	/**
	 * Generates url encoded string a EQUALS_ONE_OF filter for a multiple enum value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "radio"} in *.widget.json and {@code ? extends Enum<?>} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.radio(MyDTO_.radio, List.of(RadioEnum.VALUE));
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param values the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Enum<?>> String radio(@NotNull DtoField<? super D, T> field,
			Collection<T> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.arrayValue(
				SearchOperation.EQUALS_ONE_OF,
				true,
				field,
				values
		);
	}

	/**
	 * Generates url encoded string a SPECIFIC filter for a single boolean value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "checkbox"} in *.widget.json and {@code Boolean} type in DTO.
	 * </p>
	 *
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.checkbox(MyDTO_.checkbox, true);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO> String checkbox(@NotNull DtoField<? super D, Boolean> field, Boolean value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.SPECIFIED,
				field,
				value
		);
	}


	/**
	 * Generates url encoded string with EQUALS filter for a number value
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "money"} in *.widget.json and {@code ? extends Number} type in DTO.
	 * <br><b>Note:</b>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * CxboxFBBase<MyDTO> filterBuilder = new CxboxFBBase<>();
	 * filterBuilder.percent(MyDTO_.percent, 10);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Number> String money(@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.EQUALS,
				field,
				value
		);
	}

	/**
	 * Generates url encoded string with GREATER_OR_EQUAL_THAN and LESS_OR_EQUAL_THAN filter for a range value
	 * <p>
	 * <b>Note:</b>Use only for fields having {@code "type" : "money"} in .widget.json and {@code  ? extend Number} in DTO
	 * <br><b>Note:</b>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.money(MyDTO_.money, 1000, 10000);
	 * }</pre>
	 *</p>
	 *
	 * @param field the date field to filter (must not be {@code null})
	 * @param from the lower bound(inclusive, may be {@code null})
	 * @param to the upper bound (inclusive, may be {@code null})
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Number> String moneyFromTo(@NotNull DtoField<? super D, T> field, T from,
			T to) {
		if (from == null && to == null) {
			return null;
		}
		if (from != null && to == null) {
			return numberFrom(field, from);
		} else if (from == null) {
			return numberTo(field, to);
		} else {
			return this.numberFrom(field, from) + AMPERSAND_URL_ENCODED  + this.numberTo(field, to);
		}

	}

	/**
	 * Generates url encoded string a CONTAINS filter for a single value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "fileUpload"} in *.widget.json and {@code ? extends Serializable} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.fileUpload(MyDTO_.fileUpload, "UUID");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Serializable> String fileUpload(@NotNull DtoField<? super D, T> field,
			T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
	}

	/**
	 * Generates url encoded string a CONTAINS filter for a single value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "pickList"} in *.widget.json and {@code ? extends Serializable} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.pickList(MyDTO_.pickList,  "pickList");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Serializable> String pickList(@NotNull DtoField<? super D, T> field,
			T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
	}


	/**
	 * Generates url encoded string a CONTAINS filter for a single value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "inlinePickList"} in *.widget.json and {@code ? extends Serializable} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.inlinePickList(MyDTO_.inlinePickList,  "inlinePickList");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Serializable> String inlinePickList(
			@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
	}

	/**
	 * Generates url encoded string a CONTAINS filter for a single value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "multifield"} in *.widget.json and {@code ? extends Serializable} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.multifield(MyDTO_.multifield, "multifield");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Serializable> String multifield(@NotNull DtoField<? super D, T> field,
			T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
	}

	/**
	 * Generates url encoded string a CONTAINS filter for a single value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "suggestionPickList"} in *.widget.json and {@code ? extends Serializable} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.suggestionPickList(MyDTO_.suggestionPickList, "suggestionPickList");
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends Serializable> String suggestionPickList(
			@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.singleValue(
				SearchOperation.CONTAINS,
				field,
				value
		);
	}

	/**
	 * Generates url encoded string with EQUALS_ONE_OF filter for a multivalue
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "multivalueHover"} in *.widget.json and {@code ? extends MultivalueField} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.multivalueHover(MyDTO_.multivalueHover, multivalueHover);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field (nullable)
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends MultivalueField> String multivalueHover(
			@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.multivalue(
				SearchOperation.EQUALS_ONE_OF,
				field,
				value,
				MultivalueFieldSingleValue::getId
		);
	}


	/**
	 * Generates url encoded string with EQUALS_ONE_OF filter for a multivalue
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "multipleSelect"} in *.widget.json and {@code ? extends MultivalueField} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * platformUrlParametersFilterService.multipleSelect(MyDTO_.name, multipleSelect);
	 * }</pre>
	 * </p>
	 *
	 * @param field the DTO field to filter by
	 * @param value the value to search for in the field ({@code  nullable})
	 * @return URL-encoded fragment or null if value is null
	 */
	public <D extends DataResponseDTO, T extends MultivalueField> String multipleSelect(
			@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return null;
		}
		return DrillDownFieldFilterFormerUtils.multivalue(
				SearchOperation.EQUALS_ONE_OF,
				field,
				value,
				MultivalueFieldSingleValue::getValue
		);
	}


	/**
	 * Utils class for generation filter as encoded url string
	 */
	public static final class DrillDownFieldFilterFormerUtils {

		private DrillDownFieldFilterFormerUtils() {
			// Private constructor to prevent instantiation
		}

		/**
		 * Creates a URL-encoded filter expression for a single field/value pair.
		 *
		 * @param operation           the search operation to apply; must not be null
		 * @param field               the descriptor of the field to filter on; must not be null
		 * @param value               the value to match against the field; must not be null
		 * @param <D>                 the DTO type to which the field belongs
		 * @param <T>                 the type of the field’s value
		 * @return the URL-encoded filter expression using UTF-8 encoding
		 */
		public static <D, T> String singleValue(@NotNull SearchOperation operation,
				@NotNull DtoField<? super D, T> field,
				@NotNull T value) {
			return singleValue(operation, false, field, value);
		}

		/**
		 * Creates a URL-encoded filter expression for a single field/value pair,
		 * optionally serializing the value to JSON.
		 *
		 * @param operation           the search operation to apply; must not be null
		 * @param isJsonSerialization whether to serialize the value as JSON
		 * @param field               the descriptor of the field to filter on; must not be null
		 * @param value               the value to match against the field; must not be null
		 * @param <D>                 the DTO type to which the field belongs
		 * @param <T>                 the type of the field’s value
		 * @return the URL-encoded filter expression using UTF-8 encoding
		 */
		public static <D, T> String singleValue(@NotNull SearchOperation operation,
				boolean isJsonSerialization,
				@NotNull DtoField<? super D, T> field,
				@NotNull T value) {
			String rawValue = isJsonSerialization
					? stripQuotes(JsonUtils.writeValue(value))
					: String.valueOf(value);

			String expression = field.getName()
					+ "."
					+ operation.getOperationName()
					+ "="
					+ rawValue;

			return URLEncoder.encode(expression, StandardCharsets.UTF_8);
		}

		/**
		 * Creates a URL-encoded filter expression for a collection field/value pair.
		 *
		 * @param operation the search operation to apply; must not be null
		 * @param field     the descriptor of the field to filter on; must not be null
		 * @param values    the collection of values to match; must not be null
		 * @param <D>       the DTO type to which the field belongs
		 * @param <T>       the type of the field’s values
		 * @return the URL-encoded filter expression using UTF-8 encoding
		 */
		public static <D, T> String arrayValue(@NotNull SearchOperation operation,
				@NotNull DtoField<? super D, T> field,
				@NotNull Collection<T> values) {
			return arrayValue(operation, false, field, values);
		}

		/**
		 * Creates a URL-encoded filter expression for a collection field/value pair,
		 * optionally serializing each value to JSON.
		 *
		 * @param operation           the search operation to apply; must not be null
		 * @param isJsonSerialization whether to serialize each value as JSON
		 * @param field               the descriptor of the field to filter on; must not be null
		 * @param values              the collection of values to match; must not be null
		 * @param <D>                 the DTO type to which the field belongs
		 * @param <T>                 the type of the field’s values
		 * @return the URL-encoded filter expression using UTF-8 encoding
		 */
		public static <D, T> String arrayValue(@NotNull SearchOperation operation,
				boolean isJsonSerialization,
				@NotNull DtoField<? super D, T> field,
				@NotNull Collection<T> values) {
			String joined = values.stream()
					.map(val -> {
						String repr = isJsonSerialization
								? stripQuotes(JsonUtils.writeValue(val))
								: String.valueOf(val);
						return URLEncoder.encode(repr, StandardCharsets.UTF_8);
					})
					.collect(Collectors.joining(","));

			String expression = field.getName()
					+ "."
					+ operation.getOperationName()
					+ "=[\\\"" + joined + "\\\"]";
			return URLEncoder.encode(expression, StandardCharsets.UTF_8);
		}

		/**
		 * Creates a URL-encoded filter expression for a multivalue field.
		 *
		 * @param operation    the search operation to apply; must not be null
		 * @param field        the descriptor of the field to filter on; must not be null
		 * @param multivalue   the multivalue field instance; must not be null
		 * @param extractor    function to extract string from each single value; must not be null
		 * @param <D>          the DTO type to which the field belongs
		 * @param <T>          the multivalue field type
		 * @return the URL-encoded filter expression using UTF-8 encoding
		 */
		public static <D, T extends MultivalueField> String multivalue(@NotNull SearchOperation operation,
				@NotNull DtoField<? super D, T> field,
				@NotNull T multivalue,
				@NotNull Function<MultivalueFieldSingleValue, String> extractor) {
			String joined = multivalue.getValues().stream()
					.map(extractor)
					.map(val -> URLEncoder.encode(val, StandardCharsets.UTF_8))
					.collect(Collectors.joining("\\\",\\\""));

			String expression = field.getName()
					+ "."
					+ operation.getOperationName()
					+ "=[\\\"" + joined + "\\\"]";

			return URLEncoder.encode(expression, StandardCharsets.UTF_8);
		}

		// Helper to strip leading and trailing quotes from a JSON string
		private static String stripQuotes(String json) {
			if (json.length() >= 2 && json.startsWith("\"") && json.endsWith("\"")) {
				return json.substring(1, json.length() - 1);
			}
			return json;
		}
	}



}
