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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.multivalue.MultivalueField;
import org.cxbox.core.util.SpringBeanUtils;
import org.cxbox.dictionary.Dictionary;



/**
 * Abstract base class {@link FB} for building URL-encoded filter strings for business components.
 * <br>Short class name {@link FB}(FilterBuilder) ensures non-intrusive IntelliJ IDEA inline highlights.
 * <p>
 * Provides a fluent API for adding filters of various types (input, dictionary, date, number, etc.)
 * and assembling them into a single filter string suitable for use in platform requests.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * fb.input(MyDTO_.name, "searchText")
 *              .number(MyDTO_.id, 123L)
 *              .dateFromTo(MyDTO_.createdAt, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
 * String filterString = filterBuilder.formUrlPart(bcIdentifier);
 * }</pre>
 *
 * @param <D> DTO type (must extend {@link DataResponseDTO})
 * @param <S> Self type for fluent chaining
 */
public class FB<D extends DataResponseDTO, S extends FB<D, S>> {

	@Getter(AccessLevel.PROTECTED)
	private final PlatformDrilldownFilterService platformDrilldownFilterService;

	@Getter(AccessLevel.PROTECTED)
	private final Set<String> fieldFilters = new HashSet<>();

	/**
	 * Constructs the filter builder and injects the filter service.
	 */
	protected FB() {
		this.platformDrilldownFilterService = SpringBeanUtils.getBean(PlatformDrilldownFilterService.class);
	}

	/**
	 * Adds a filter for a String input field ("type": "input")
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dictionary"} in *.widget.json and {@code ? implements Dictionary} type in DTO.
	 * </p>
	 * <p>
	 * Example usage with default implementation {@link FB}:
	 * <pre>{@code
	 * fb.input(MyDTO_.input, "input");
	 * }</pre>
	 * </p>
	 *
	 * @param field DTO field
	 * @param value value to filter by (nullable)
	 * @return builder {@link S} for fluent api
	 */
	public S input(@NonNull DtoField<? super D, String> field, @Nullable String value) {
		if (value == null) {
			return this.self();
		}
		return this.add(platformDrilldownFilterService.input(field, value));
	}

	/**
	 *  Adds a filter for a dictionary field ("type": "dictionary").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dictionary"} in *.widget.json and {@code ? implements Dictionary} type in DTO.
	 * </p>
	 * <p>
	 * Example usage with default implementation {@link FB}:
	 * <pre>{@code
	 * fb.dictionary(MyDTO_.dictionary, MyDictionary.VALUE);
	 * }</pre>
	 * </p>
	 *
	 * @param field DTO field
	 * @param value dictionary value (nullable)
	 * @return this builder {@link FB} instance for fluent chaining
	 */
	public <T extends Dictionary> S dictionary(@NonNull DtoField<? super D, T> field, @Nullable T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.dictionary(field, value));
	}

	/**
	 * Adds a filter for a dictionary field with multiple values.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dictionary"} in *.widget.json and {@code ? implements Dictionary} type in DTO.
	 * </p>
	 * <p>
	 * Example usage with default implementation {@link FB}:
	 * <pre>{@code
	 * fb.dictionary(MyDTO_.dictionary, List.of(MyDictionary.VALUE1,of(MyDictionary.VALUE1));
	 * }</pre>
	 * </p>
	 *
	 * @param field DTO field
	 * @param values collection of dictionary values (nullable or empty)
	 * @return this builder
	 */
	public <T extends Dictionary> S dictionary(@NonNull DtoField<? super D, T> field, @Nullable Collection<T> values) {
		if (values == null || values.isEmpty()) {
			return self();
		}
		return this.add(platformDrilldownFilterService.dictionary(field, values));

	}

	/**
	 *  Adds a filter for a dictionary field ("type": "dictionary").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dictionary"} in *.widget.json and {@code ? extends Enum<?>} type in DTO.
	 * </p>
	 * <p>
	 * Example usage with default implementation {@link FB}:
	 * <pre>{@code
	 * fb.dictionary(MyDTO_.dictionary, MyDictionary.VALUE);
	 * }</pre>
	 * </p>
	 *
	 * @param field DTO field
	 * @param value dictionary value (nullable)
	 * @return this builder {@link FB} instance for fluent chaining
	 */
	public <T extends Enum<?>> S dictionaryEnum(@NonNull DtoField<? super D, T> field, @Nullable T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.dictionaryEnum(field, value));
	}


	/**
	 * Adds a filter for an enum dictionary field ("type": "dictionary") with multiple values.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dictionary"} in *.widget.json and {@code ? extends Enum<?>} type in DTO.
	 * </p>
	 * <p>
	 * Example usage with default implementation {@link FB}:
	 * <pre>{@code
	 * fb.dictionary(MyDTO_.dictionary, List.of(MyDictionaryEnum.VALUE1,MyDictionaryEnum.VALUE2));
	 * }</pre>
	 * </p>
	 *
	 * @param field DTO field
	 * @param values collection of dictionary values (nullable or empty)
	 * @return this builder {@link FB} instance for fluent chaining
	 */
	public <T extends Enum<?>> S dictionaryEnum(@NonNull DtoField<? super D, T> field, @Nullable Collection<T> values) {
		if (values == null || values.isEmpty()) {
			return self();
		}
		return this.add(platformDrilldownFilterService.dictionaryEnum(field, values));
	}


	/**
	 * Adds a filter for a date field ("type": "date"), single value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "date"} in *.widget.json and {@code LocalDateTime} type in DTO.
	 * <br><b>Note:</b> Use only if disabled property filter-by-range-enabled-default {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * </p>
	 * <p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.date(MyDTO_.date, LocalDate.now());
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value date value (nullable)
	 * @return this builder
	 */
	public S date(@NonNull DtoField<? super D, LocalDateTime> field, @Nullable LocalDate value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.date(field, value));
	}

	/**
	 * Adds a range filter for a date field ("type": "date").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "date"} in *.widget.json and {@code LocalDateTime} type in DTO.
	 * <br><b>Note:</b>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * </p>
	 * <p>
	 *<p>
	 * Example usage:
	 * <pre>{@code
	 * fb.dateFromTo(MyDTO_.date,
	 *     LocalDate.of(2025, 1, 1),
	 *     LocalDate.of(2025, 1, 31))
	 * }</pre>
	 *
	 *</p>
	 * @param field DTO field
	 * @param from start date (inclusive, nullable)
	 * @param to end date (inclusive, nullable)
	 * @return this builder
	 */
	public S dateFromTo(@NonNull DtoField<? super D, LocalDateTime> field, @Nullable LocalDate from,
			@Nullable LocalDate to) {
		if (from == null && to == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.dateFromTo(field, from, to));
	}

	/**
	 * Adds a filter for a dateTime field ("type": "dateTime"), single value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "dateTime"} in *.widget.json and {@code LocalDateTime} type in DTO.
	 * <br><b>Note:</b>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.dateTime(MyDTO_.dateTime, LocalDate.now());
	 * }</pre>
	 * </p>
	 *
	 * @param field DTO field
	 * @param value date-time value (nullable)
	 * @return this builder
	 */
	public S dateTime(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDateTime value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.dateTime(field, value));
	}

	/**
	 * Adds a range filter for a dateTime field ("type": "dateTime").
	 * <p>
	 * <b>Note:</b> Use for fields having {@code "type": "dateTime"} in *.widget.json and {@code LocalDateTime} type in DTO.
	 * <br><b>Note:</b> Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.dateFromTo(Entity_.dateTime,
	 *     LocalDate.now().minusYear(1),
	 *     LocalDate.now().plusYear(1))
	 * }</pre>
	 *
	 * @param field DTO field
	 * @param from start datetime (inclusive, nullable)
	 * @param to end datetime (inclusive, nullable)
	 * @return this builder
	 */
	public S dateTimeFromTo(@NonNull DtoField<? super D, LocalDateTime> field,
			@Nullable LocalDateTime from,
			@Nullable LocalDateTime to) {
		if (from == null && to == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.dateTimeFromTo(field, from, to));
	}


	/**
	 * Adds a filter for a multivalue field ("type": "multivalue").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "multivalue"} in *.widget.json and {@code MultivalueField} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.multivalue(MyDTO_.multivalue, myMultivalue);
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value multivalue field (nullable)
	 * @return this builder
	 */
	public S multiValue(@NonNull DtoField<? super D, MultivalueField> field,
			@Nullable MultivalueField value) {
		if (value == null) {
			return self();
		}
		return this.add(this.platformDrilldownFilterService.multiValue(field, value));
	}

	/**
	 * Adds a filter for a number field ("type": "number"), single value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "number"} in *.widget.json and {@code ? extends Number} type in DTO.
	 * <br><b>Note:</b>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.number(MyDTO_.number, 1L);
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value number value (nullable)
	 * @return this builder
	 */
	public <T extends Number> S number(@NonNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.number(field, value));
	}

	/**
	 * Adds a range filter for a number field ("type": "number").
	 * <p>
	 * <b>Note:</b>Use only for fields having {@code "type" : "number"} in .widget.json and {@code  ? extend Number} in DTO
	 * <br><b>Note:</b>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * </p>
	 * Example usage:
	 * <pre>{@code
	 * fb.number(MyDTO_.number, 1,10);
	 * }</pre>
	 * <p>
	 * @param field DTO field
	 * @param from lower bound (inclusive, nullable)
	 * @param to upper bound (inclusive, nullable)
	 * @return this builder
	 */
	public <T extends Number> S numberFromTo(@NonNull DtoField<? super D, T> field, @Nullable T from,
			@Nullable T to) {
		if (from == null && to == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.numberFromTo(field, from, to));
	}

	/**
	 * Adds a filter for a percent field ("type": "percent"), single value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "percent"} in *.widget.json and {@code ? extends Number} type in DTO.
	 * <br><b>Note:</b>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.percent(MyDTO_.percent, 10L);
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value percent value (nullable)
	 * @return this builder
	 */
	public <T extends Number> S percent(@NonNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.percent(field, value));
	}


	/**
	 * Adds a range filter for a percent field ("type": "percentFromTo").
	 * <p>
	 * <b>Note:</b>Use only for fields having {@code "type" : "percentFromTo"} in .widget.json and {@code  ? extend Number} in DTO
	 * <br><b>Note:</b>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.percentFromTo(MyDTO_.percentFromTo, 5L, 10L);
	 * }</pre>
	 * </p>
	 *
	 * @param field DTO field
	 * @param from lower bound (inclusive, nullable)
	 * @param to upper bound (inclusive, nullable)
	 * @return this builder
	 */
	@NonNull
	public <T extends Number> S percentFromTo(@NonNull DtoField<? super D, T> field, T from, T to) {
		if (from == null && to == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.percentFromTo(field, from, to));
	}


	/**
	 * Adds a filter for a text field ("type": "text").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "text"} in *.widget.json and {@code String} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * CxboxFBBase<MyDTO> filterBuilder = new CxboxFBBase<>();
	 * fb.text(MyDTO_.text, "searchText");
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value text value (nullable)
	 * @return this builder
	 */
	public S text(@NonNull DtoField<? super D, String> field, String value) {
		if (value == null) {
			return this.self();
		}
		return this.add(platformDrilldownFilterService.text(field, value));
	}

	/**
	 * Adds a filter for a radio field ("type": "radio").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "radio"} in *.widget.json and {@code ? extends Enum<?>} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.radio(MyDTO_.radio, List.of(RadioEnum.VALUE));
	 * }</pre>
	 * </p>
	 * <p>
	 * @param field DTO field
	 * @param values radio values (nullable or empty)
	 * @return this builder
	 */
	public <T extends Enum<?>> S radio(@NotNull DtoField<? super D, T> field, Collection<T> values) {
		if (values == null || values.isEmpty()) {
			return self();
		}
		return this.add(platformDrilldownFilterService.radio(field, values));
	}

	/**
	 * Adds a filter for a checkbox field ("type": "checkbox").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "checkbox"} in *.widget.json and {@code Boolean} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.checkbox(MyDTO_.checkbox, true);
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value boolean value (nullable)
	 * @return this builder
	 */
	public S checkbox(@NotNull DtoField<? super D, Boolean> field, Boolean value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.checkbox(field, value));
	}

	/**
	 * Adds a filter for a money field ("type": "money"), single value.
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "money"} in *.widget.json and {@code ? extends Number} type in DTO.
	 * <br><b>Note:</b>Use only if disabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=false}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.money(MyDTO_.money, 10000L);
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value money value (nullable)
	 * @return this builder
	 */
	public <T extends Number> S money(@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.money(field, value));
	}

	/**
	 * Adds a range filter for a money field ("type": "moneyFromTo").
	 * <p>
	 * <b>Note:</b>Use only for fields having {@code "type" : "moneyFromTo"} in .widget.json and {@code  ? extend Number} in DTO
	 * <br><b>Note:</b>Use only if enabled property filter-by-range-enabled-default  {@code cxbox.widget.fields.filter-by-range-enabled-default=true}
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.moneyFromTo(MyDTO_.moneyFromTo, 5000L, 10000L);
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param from lower bound (inclusive, nullable)
	 * @param to upper bound (inclusive, nullable)
	 * @return this builder
	 */
	public <T extends Number> S moneyFromTo(@NotNull DtoField<? super D, T> field, T from, T to) {
		if (from == null && to == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.moneyFromTo(field, from, to));

	}

	/**
	 * Adds a filter for a file upload field ("type": "fileUpload").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "fileUpload"} in *.widget.json and {@code ? extends Serializable} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.fileUpload(MyDTO_.fileUpload,  "UUID");
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value file value (nullable)
	 * @return this builder
	 */
	public <T extends Serializable> S fileUpload(@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.fileUpload(field, value));
	}

	/**
	 * Adds a filter for a pick list field ("type": "pickList").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "pickList"} in *.widget.json and {@code ? extends Serializable} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.pickList(MyDTO_.pickList,  "pickList");
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value pick list value (nullable)
	 * @return this builder
	 */
	public <T extends Serializable> S pickList(@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.pickList(field, value));
	}

	/**
	 * Adds a filter for an inline pick list field ("type": "inlinePickList").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "inlinePickList"} in *.widget.json and {@code ? extends Serializable} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.inlinePickList(MyDTO_.inlinePickList,  "inlinePickList");
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value pick list value (nullable)
	 * @return this builder
	 */
	public <T extends Serializable> S inlinePickList(@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.inlinePickList(field, value));
	}


	/**
	 * Adds a filter for a multifield field ("type": "multifield").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "multifield"} in *.widget.json and {@code ? extends Serializable} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.inlinePickList(MyDTO_.inlinePickList, "inlinePickList");
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value multifield value (nullable)
	 * @return this builder
	 */
	public <T extends Serializable> S multifield(@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.multifield(field, value));
	}

	/**
	 * Adds a filter for a suggestion pick list field ("type": "suggestionPickList").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "suggestionPickList"} in *.widget.json and {@code ? extends Serializable} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.suggestionPickList(MyDTO_.suggestionPickList, "suggestionPickList
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value suggestion value (nullable)
	 * @return this builder
	 */
	public <T extends Serializable> S suggestionPickList(@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.suggestionPickList(field, value));
	}

	/**
	 * Adds a filter for a multivalue hover field ("type": "multivalueHover").
	 *
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "multivalueHover"} in *.widget.json and {@code ? extends MultivalueField} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.multivalueHover(MyDTO_.multivalueHover, multivalueHoverValue);
	 * }</pre>
	 * </p>
	 *
	 * @param field DTO field
	 * @param value multivalue value (nullable)
	 * @return this builder
	 */
	public <T extends MultivalueField> S multivalueHover(@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.multivalueHover(field, value));
	}

	/**
	 * Adds a filter for a multiple select field ("type": "multipleSelect").
	 * <p>
	 * <b>Note:</b> Use only for fields having {@code "type": "multipleSelect"} in *.widget.json and {@code ? extends MultivalueField} type in DTO.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * fb.multipleSelect(MyDTO_.multipleSelect, multipleSelectValue);
	 * }</pre>
	 * </p>
	 * @param field DTO field
	 * @param value multivalue value (nullable)
	 * @return this builder
	 */
	public <T extends MultivalueField> S multipleSelect(@NotNull DtoField<? super D, T> field, T value) {
		if (value == null) {
			return self();
		}
		return this.add(platformDrilldownFilterService.multipleSelect(field, value));
	}

	/**
	 * Adds a  filter string to this builder.
	 *
	 * @param value filter string (nullable or empty ignored)
	 * @return this builder
	 */
	protected S add(String value) {
		if (value == null || value.isEmpty()) {
			return self();
		}
		this.fieldFilters.add(value);
		return self();
	}

	/**
	 * Returns this instance as the subclass type for fluent chaining.
	 *
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	protected S self() {
		return (S) this;
	}

}
