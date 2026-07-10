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

import static org.assertj.core.api.Assertions.assertThat;
import static org.cxbox.core.controller.param.SearchOperation.CONTAINS;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS_ONE_OF;
import static org.cxbox.core.controller.param.SearchOperation.GREATER_OR_EQUAL_THAN;
import static org.cxbox.core.controller.param.SearchOperation.LESS_OR_EQUAL_THAN;
import static org.cxbox.core.controller.param.SearchOperation.SPECIFIED;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.config.JacksonConfig;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.controller.param.SearchOperation;
import org.cxbox.core.dto.multivalue.MultivalueField;
import org.cxbox.core.dto.multivalue.MultivalueFieldSingleValue;
import org.cxbox.core.util.SpringBeanUtils;
import org.cxbox.dictionary.Dictionary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Every filter of {@link PlatformDrilldownFilterService} is checked twice: as the exact expression it puts into
 * the drilldown url, and through {@link #replay(String...)} - the way this expression comes back to the backend.
 */
@DirtiesContext
@SpringJUnitConfig({
		JacksonConfig.class,
		SpringBeanUtils.class
})
class PlatformDrilldownFilterServiceTest {

	private static final BcIdentifier BC = new TestBc("testBc");

	private static final LocalDate DAY = LocalDate.of(2025, 7, 16);

	private static final LocalDateTime MOMENT = LocalDateTime.of(2025, 7, 16, 9, 25, 55);

	private final PlatformDrilldownFilterService service = new PlatformDrilldownFilterService();

	private static String decode(String urlEncoded) {
		return URLDecoder.decode(urlEncoded, StandardCharsets.UTF_8);
	}

	private static MultivalueField multivalue(String... idsAndValues) {
		List<MultivalueFieldSingleValue> values = new ArrayList<>();
		for (int i = 0; i < idsAndValues.length; i += 2) {
			values.add(new MultivalueFieldSingleValue(idsAndValues[i], idsAndValues[i + 1]));
		}
		return new MultivalueField(values);
	}

	/**
	 * Replays the way of a filter from the drilldown url back to the backend:
	 * <ol>
	 *   <li>the ui reads the {@code filters} query parameter (url decoding) and json-parses it into
	 *   {@code bcName -> filter string};</li>
	 *   <li>the ui treats the filter string of a bc as a query string ({@code URLSearchParams}: split by
	 *   {@code &}, url decoding once more) and sends every pair back as a request parameter;</li>
	 *   <li>the backend builds a {@link FilterParameter} out of every request parameter.</li>
	 * </ol>
	 */
	private List<FilterParameter> replay(String... filters) {
		String filtersQueryParameter = "{" + service.formUrlPart(BC, Arrays.asList(filters)).orElseThrow() + "}";
		Map<String, String> filterStringByBc;
		try {
			filterStringByBc = new ObjectMapper().readValue(decode(filtersQueryParameter), new TypeReference<>() {
			});
		} catch (Exception e) {
			throw new IllegalStateException("filters query parameter is not a json: " + filtersQueryParameter, e);
		}
		return Arrays.stream(filterStringByBc.get(BC.getName()).split("&"))
				.map(pair -> FilterParameter.Builder.getInstance().buildParameter(
						decode(pair.substring(0, pair.indexOf('='))),
						decode(pair.substring(pair.indexOf('=') + 1))
				))
				.toList();
	}

	private FilterParameter replaySingle(String filter) {
		List<FilterParameter> parameters = replay(filter);
		assertThat(parameters).hasSize(1);
		return parameters.get(0);
	}

	private void assertSingleValue(String filter, String field, SearchOperation operation, String value) {
		assertThat(decode(filter)).isEqualTo(field + "." + operation.getOperationName() + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
		FilterParameter parameter = replaySingle(filter);
		assertThat(parameter.getName()).isEqualTo(field);
		assertThat(parameter.getOperation()).isEqualTo(operation);
		assertThat(parameter.getStringValue()).isEqualTo(value);
	}

	private void assertOneOf(String filter, String field, String... values) {
		FilterParameter parameter = replaySingle(filter);
		assertThat(parameter.getName()).isEqualTo(field);
		assertThat(parameter.getOperation()).isEqualTo(EQUALS_ONE_OF);
		assertThat(parameter.getStringValuesAsList()).containsExactly(values);
	}

	private void assertRange(String filter, String field, String from, String to) {
		List<FilterParameter> parameters = replay(filter);
		List<String> expected = new ArrayList<>();
		if (from != null) {
			expected.add(field + "." + GREATER_OR_EQUAL_THAN.getOperationName() + "=" + from);
		}
		if (to != null) {
			expected.add(field + "." + LESS_OR_EQUAL_THAN.getOperationName() + "=" + to);
		}
		assertThat(parameters)
				.extracting(p -> p.getName() + "." + p.getOperation().getOperationName() + "=" + p.getStringValue())
				.containsExactlyElementsOf(expected);
	}

	@Nested
	class FormUrlPart {

		@Test
		void joinsFiltersOfOneBcWithEncodedAmpersand() {
			assertThat(service.formUrlPart(BC, List.of("a.equals=1", "b.equals=2")))
					.contains("\"testBc\":\"a.equals=1%26b.equals=2\"");
		}

		@Test
		void skipsNullAndEmptyFilters() {
			assertThat(service.formUrlPart(BC, Arrays.asList(null, "a.equals=1", "")))
					.contains("\"testBc\":\"a.equals=1\"");
		}

		@Test
		void givesNothingWhenThereIsNoFilter() {
			assertThat(service.formUrlPart(BC, Arrays.asList(null, ""))).isEmpty();
			assertThat(service.formUrlPart(BC, List.of())).isEmpty();
		}

		@Test
		void everyFilterOfSeveralFieldsComesBackToTheBackend() {
			List<FilterParameter> parameters = replay(
					service.input(TestDrilldownDto_.input, "abc"),
					service.checkbox(TestDrilldownDto_.checkbox, true),
					service.dictionaryEnum(TestDrilldownDto_.dictionaryEnum, List.of(TestEnum.FIRST, TestEnum.SECOND)),
					service.numberFromTo(TestDrilldownDto_.number, 1L, 5L)
			);

			assertThat(parameters)
					.extracting(p -> p.getName() + "." + p.getOperation().getOperationName() + "=" + p.getStringValue())
					.containsExactly(
							"input.contains=abc",
							"checkbox.specified=true",
							"dictionaryEnum.equalsOneOf=[\"FIRST\",\"SECOND\"]",
							"number.greaterOrEqualThan=1",
							"number.lessOrEqualThan=5"
					);
		}

	}

	@Nested
	class Input {

		@Test
		void containsValue() {
			assertSingleValue(service.input(TestDrilldownDto_.input, "abc"), "input", CONTAINS, "abc");
		}

		@Test
		void keepsSpacesAndNonLatinLetters() {
			assertSingleValue(service.input(TestDrilldownDto_.input, "Иван Petrov"), "input", CONTAINS, "Иван Petrov");
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.input(TestDrilldownDto_.input, null)).isNull();
		}

	}

	@Nested
	class Text {

		@Test
		void containsValue() {
			assertSingleValue(service.text(TestDrilldownDto_.text, "long text"), "text", CONTAINS, "long text");
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.text(TestDrilldownDto_.text, null)).isNull();
		}

	}

	@Nested
	class FileUpload {

		@Test
		void containsFileName() {
			assertSingleValue(
					service.fileUpload(TestDrilldownDto_.fileUpload, "report 2025.pdf"), "fileUpload", CONTAINS, "report 2025.pdf"
			);
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.fileUpload(TestDrilldownDto_.fileUpload, null)).isNull();
		}

	}

	@Nested
	class PickList {

		@Test
		void containsValue() {
			assertSingleValue(service.pickList(TestDrilldownDto_.pickList, "PL1"), "pickList", CONTAINS, "PL1");
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.pickList(TestDrilldownDto_.pickList, null)).isNull();
		}

	}

	@Nested
	class InlinePickList {

		@Test
		void containsValue() {
			assertSingleValue(
					service.inlinePickList(TestDrilldownDto_.inlinePickList, "IPL1"), "inlinePickList", CONTAINS, "IPL1"
			);
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.inlinePickList(TestDrilldownDto_.inlinePickList, null)).isNull();
		}

	}

	@Nested
	class Multifield {

		@Test
		void containsValue() {
			assertSingleValue(service.multifield(TestDrilldownDto_.multifield, "MF1"), "multifield", CONTAINS, "MF1");
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.multifield(TestDrilldownDto_.multifield, null)).isNull();
		}

	}

	@Nested
	class SuggestionPickList {

		@Test
		void containsValue() {
			assertSingleValue(
					service.suggestionPickList(TestDrilldownDto_.suggestionPickList, "SPL1"), "suggestionPickList", CONTAINS, "SPL1"
			);
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.suggestionPickList(TestDrilldownDto_.suggestionPickList, null)).isNull();
		}

	}

	@Nested
	class DictionaryField {

		@Test
		void oneValue() {
			String filter = service.dictionary(TestDrilldownDto_.dictionary, new TestDictionary("HIGH"));

			assertThat(decode(filter)).isEqualTo("dictionary.equalsOneOf=[\\\"HIGH\\\"]");
			assertOneOf(filter, "dictionary", "HIGH");
		}

		/**
		 * The values used to be joined by a bare {@code ,} inside a single pair of escaped quotes,
		 * so the whole collection was parsed back as one value {@code "HIGH,LOW"}.
		 */
		@Test
		void severalValuesStaySeparate() {
			String filter = service.dictionary(
					TestDrilldownDto_.dictionary,
					List.of(new TestDictionary("HIGH"), new TestDictionary("LOW"))
			);

			assertThat(decode(filter)).isEqualTo("dictionary.equalsOneOf=[\\\"HIGH\\\",\\\"LOW\\\"]");
			assertOneOf(filter, "dictionary", "HIGH", "LOW");
		}

		@Test
		void valuesWithSpacesCommasAndNonLatinLettersStaySeparate() {
			String filter = service.dictionary(
					TestDrilldownDto_.dictionary,
					List.of(new TestDictionary("In progress"), new TestDictionary("Done, archived"), new TestDictionary("Новый"))
			);

			assertOneOf(filter, "dictionary", "In progress", "Done, archived", "Новый");
		}

		@Test
		void nullAndEmptyGiveNoFilter() {
			assertThat(service.dictionary(TestDrilldownDto_.dictionary, (TestDictionary) null)).isNull();
			assertThat(service.dictionary(TestDrilldownDto_.dictionary, (List<TestDictionary>) null)).isNull();
			assertThat(service.dictionary(TestDrilldownDto_.dictionary, List.of())).isNull();
		}

	}

	@Nested
	class DictionaryEnumField {

		@Test
		void oneValueIsTheEnumName() {
			String filter = service.dictionaryEnum(TestDrilldownDto_.dictionaryEnum, TestEnum.FIRST);

			assertThat(decode(filter)).isEqualTo("dictionaryEnum.equalsOneOf=[\\\"FIRST\\\"]");
			assertOneOf(filter, "dictionaryEnum", "FIRST");
		}

		/** See {@link DictionaryField#severalValuesStaySeparate()}. */
		@Test
		void severalValuesStaySeparate() {
			String filter = service.dictionaryEnum(TestDrilldownDto_.dictionaryEnum, List.of(TestEnum.FIRST, TestEnum.SECOND));

			assertThat(decode(filter)).isEqualTo("dictionaryEnum.equalsOneOf=[\\\"FIRST\\\",\\\"SECOND\\\"]");
			assertOneOf(filter, "dictionaryEnum", "FIRST", "SECOND");
		}

		@Test
		void jsonValueOfTheEnumIsUsedInsteadOfItsName() {
			String one = service.dictionaryEnum(TestDrilldownDto_.dictionaryJsonValueEnum, TestJsonValueEnum.IN_PROGRESS);
			String several = service.dictionaryEnum(
					TestDrilldownDto_.dictionaryJsonValueEnum,
					List.of(TestJsonValueEnum.IN_PROGRESS, TestJsonValueEnum.DONE)
			);

			assertOneOf(one, "dictionaryJsonValueEnum", "In progress");
			assertOneOf(several, "dictionaryJsonValueEnum", "In progress", "Done");
		}

		@Test
		void nullAndEmptyGiveNoFilter() {
			assertThat(service.dictionaryEnum(TestDrilldownDto_.dictionaryEnum, (TestEnum) null)).isNull();
			assertThat(service.dictionaryEnum(TestDrilldownDto_.dictionaryEnum, (List<TestEnum>) null)).isNull();
			assertThat(service.dictionaryEnum(TestDrilldownDto_.dictionaryEnum, List.of())).isNull();
		}

	}

	@Nested
	class Radio {

		@Test
		void oneValue() {
			assertOneOf(service.radio(TestDrilldownDto_.radio, List.of(TestEnum.FIRST)), "radio", "FIRST");
		}

		/** See {@link DictionaryField#severalValuesStaySeparate()}. */
		@Test
		void severalValuesStaySeparate() {
			String filter = service.radio(TestDrilldownDto_.radio, List.of(TestEnum.FIRST, TestEnum.SECOND));

			assertThat(decode(filter)).isEqualTo("radio.equalsOneOf=[\\\"FIRST\\\",\\\"SECOND\\\"]");
			assertOneOf(filter, "radio", "FIRST", "SECOND");
		}

		@Test
		void nullAndEmptyGiveNoFilter() {
			assertThat(service.radio(TestDrilldownDto_.radio, null)).isNull();
			assertThat(service.radio(TestDrilldownDto_.radio, List.of())).isNull();
		}

	}

	@Nested
	class Checkbox {

		@Test
		void checked() {
			String filter = service.checkbox(TestDrilldownDto_.checkbox, true);

			assertSingleValue(filter, "checkbox", SPECIFIED, "true");
			assertThat(replaySingle(filter).getBooleanValue()).isTrue();
		}

		@Test
		void unchecked() {
			String filter = service.checkbox(TestDrilldownDto_.checkbox, false);

			assertSingleValue(filter, "checkbox", SPECIFIED, "false");
			assertThat(replaySingle(filter).getBooleanValue()).isFalse();
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.checkbox(TestDrilldownDto_.checkbox, null)).isNull();
		}

	}

	@Nested
	class Number {

		@Test
		void equalsValue() {
			String filter = service.number(TestDrilldownDto_.number, 150L);

			assertSingleValue(filter, "number", EQUALS, "150");
			assertThat(replaySingle(filter).getLongValue()).isEqualTo(150L);
		}

		@Test
		void negativeAndFractionalValuesKeepTheirForm() {
			assertSingleValue(service.number(TestDrilldownDto_.number, -7L), "number", EQUALS, "-7");
			assertSingleValue(service.number(TestDrilldownDto_.money, new BigDecimal("10.50")), "money", EQUALS, "10.50");
		}

		@Test
		void range() {
			assertRange(service.numberFromTo(TestDrilldownDto_.number, 1L, 5L), "number", "1", "5");
			assertRange(service.numberFromTo(TestDrilldownDto_.number, 1L, null), "number", "1", null);
			assertRange(service.numberFromTo(TestDrilldownDto_.number, null, 5L), "number", null, "5");
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.number(TestDrilldownDto_.number, null)).isNull();
			assertThat(service.numberFromTo(TestDrilldownDto_.number, null, null)).isNull();
		}

	}

	@Nested
	class Percent {

		@Test
		void equalsValue() {
			assertSingleValue(service.percent(TestDrilldownDto_.percent, 20L), "percent", EQUALS, "20");
		}

		@Test
		void range() {
			assertRange(service.percentFromTo(TestDrilldownDto_.percent, 20L, 80L), "percent", "20", "80");
			assertRange(service.percentFromTo(TestDrilldownDto_.percent, 20L, null), "percent", "20", null);
			assertRange(service.percentFromTo(TestDrilldownDto_.percent, null, 80L), "percent", null, "80");
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.percent(TestDrilldownDto_.percent, null)).isNull();
			assertThat(service.percentFromTo(TestDrilldownDto_.percent, null, null)).isNull();
		}

	}

	@Nested
	class Money {

		@Test
		void equalsValue() {
			String filter = service.money(TestDrilldownDto_.money, new BigDecimal("100.25"));

			assertSingleValue(filter, "money", EQUALS, "100.25");
			assertThat(replaySingle(filter).getBigDecimalValue()).isEqualByComparingTo("100.25");
		}

		@Test
		void range() {
			BigDecimal from = new BigDecimal("100.00");
			BigDecimal to = new BigDecimal("500.50");

			assertRange(service.moneyFromTo(TestDrilldownDto_.money, from, to), "money", "100.00", "500.50");
			assertRange(service.moneyFromTo(TestDrilldownDto_.money, from, null), "money", "100.00", null);
			assertRange(service.moneyFromTo(TestDrilldownDto_.money, null, to), "money", null, "500.50");
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.money(TestDrilldownDto_.money, null)).isNull();
			assertThat(service.moneyFromTo(TestDrilldownDto_.money, null, null)).isNull();
		}

	}

	@Nested
	class Date {

		@Test
		void oneDayIsTheRangeFromItsFirstToItsLastSecond() {
			String filter = service.date(TestDrilldownDto_.date, DAY);

			assertRange(filter, "date", "2025-07-16T00:00:00", "2025-07-16T23:59:59");
			assertThat(replay(filter)).extracting(FilterParameter::getDateValue)
					.containsExactly(DAY.atStartOfDay(), DAY.atTime(23, 59, 59));
		}

		@Test
		void range() {
			LocalDate to = DAY.plusDays(2);

			assertRange(service.dateFromTo(TestDrilldownDto_.date, DAY, to), "date", "2025-07-16T00:00:00", "2025-07-18T23:59:59");
			assertRange(service.dateFromTo(TestDrilldownDto_.date, DAY, null), "date", "2025-07-16T00:00:00", null);
			assertRange(service.dateFromTo(TestDrilldownDto_.date, null, to), "date", null, "2025-07-18T23:59:59");
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.date(TestDrilldownDto_.date, null)).isNull();
			assertThat(service.dateFromTo(TestDrilldownDto_.date, null, null)).isNull();
		}

	}

	@Nested
	class DateTime {

		@Test
		void oneMomentIsTheRangeFromItToItself() {
			String filter = service.dateTime(TestDrilldownDto_.dateTime, MOMENT);

			assertRange(filter, "dateTime", "2025-07-16T09:25:55", "2025-07-16T09:25:55");
			assertThat(replay(filter)).extracting(FilterParameter::getDateValue).containsExactly(MOMENT, MOMENT);
		}

		@Test
		void range() {
			LocalDateTime to = MOMENT.plusHours(3);

			assertRange(
					service.dateTimeFromTo(TestDrilldownDto_.dateTime, MOMENT, to), "dateTime", "2025-07-16T09:25:55", "2025-07-16T12:25:55"
			);
			assertRange(service.dateTimeFromTo(TestDrilldownDto_.dateTime, MOMENT, null), "dateTime", "2025-07-16T09:25:55", null);
			assertRange(service.dateTimeFromTo(TestDrilldownDto_.dateTime, null, to), "dateTime", null, "2025-07-16T12:25:55");
		}

		@Test
		void nullGivesNoFilter() {
			assertThat(service.dateTime(TestDrilldownDto_.dateTime, null)).isNull();
			assertThat(service.dateTimeFromTo(TestDrilldownDto_.dateTime, null, null)).isNull();
		}

	}

	@Nested
	class MultiValue {

		@Test
		void filtersByIdsOfTheValues() {
			String filter = service.multiValue(TestDrilldownDto_.multiValue, multivalue("1", "First", "2", "Second"));

			assertThat(decode(filter)).isEqualTo("multiValue.equalsOneOf=[\\\"1\\\",\\\"2\\\"]");
			assertOneOf(filter, "multiValue", "1", "2");
		}

		@Test
		void nullAndEmptyGiveNoFilter() {
			assertThat(service.multiValue(TestDrilldownDto_.multiValue, null)).isNull();
			assertThat(service.multiValue(TestDrilldownDto_.multiValue, multivalue())).isNull();
		}

	}

	@Nested
	class MultivalueHover {

		@Test
		void filtersByIdsOfTheValues() {
			String filter = service.multivalueHover(TestDrilldownDto_.multivalueHover, multivalue("1", "First", "2", "Second"));

			assertOneOf(filter, "multivalueHover", "1", "2");
		}

		@Test
		void nullAndEmptyGiveNoFilter() {
			assertThat(service.multivalueHover(TestDrilldownDto_.multivalueHover, null)).isNull();
			assertThat(service.multivalueHover(TestDrilldownDto_.multivalueHover, multivalue())).isNull();
		}

	}

	@Nested
	class MultipleSelect {

		@Test
		void filtersByTheValuesNotByTheirIds() {
			String filter = service.multipleSelect(TestDrilldownDto_.multipleSelect, multivalue("1", "First", "2", "Second"));

			assertThat(decode(filter)).isEqualTo("multipleSelect.equalsOneOf=[\\\"First\\\",\\\"Second\\\"]");
			assertOneOf(filter, "multipleSelect", "First", "Second");
		}

		@Test
		void valuesWithSpacesStaySeparate() {
			String filter = service.multipleSelect(
					TestDrilldownDto_.multipleSelect, multivalue("1", "In progress", "2", "Done, archived")
			);

			assertOneOf(filter, "multipleSelect", "In progress", "Done, archived");
		}

		@Test
		void nullAndEmptyGiveNoFilter() {
			assertThat(service.multipleSelect(TestDrilldownDto_.multipleSelect, null)).isNull();
			assertThat(service.multipleSelect(TestDrilldownDto_.multipleSelect, multivalue())).isNull();
		}

	}

	/**
	 * A single value is url encoded on its own before the whole expression is url encoded, the same way as every
	 * value of a collection - otherwise the ui splits the filter string of a bc as a query string and the whole
	 * thing travels inside a hand-built json, so {@code &}, {@code +}, {@code %}, {@code "} and {@code \} inside
	 * a value would not survive.
	 */
	@Nested
	class SpecialCharactersInSingleValue {

		@Test
		void ampersand() {
			assertThat(replaySingle(service.input(TestDrilldownDto_.input, "Johnson & Johnson")).getStringValue())
					.isEqualTo("Johnson & Johnson");
		}

		@Test
		void plus() {
			assertThat(replaySingle(service.input(TestDrilldownDto_.input, "1+1")).getStringValue()).isEqualTo("1+1");
		}

		@Test
		void percent() {
			assertThat(replaySingle(service.text(TestDrilldownDto_.text, "50% off")).getStringValue()).isEqualTo("50% off");
		}

		@Test
		void quoteAndBackslash() {
			assertThat(replaySingle(service.input(TestDrilldownDto_.input, "say \"hi\"")).getStringValue())
					.isEqualTo("say \"hi\"");
			assertThat(replaySingle(service.fileUpload(TestDrilldownDto_.fileUpload, "C:\\temp\\a.pdf")).getStringValue())
					.isEqualTo("C:\\temp\\a.pdf");
		}

	}

	record TestBc(String name) implements BcIdentifier {

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getParentName() {
			return null;
		}

	}

	record TestDictionary(String key) implements Dictionary {

	}

	enum TestEnum {
		FIRST,
		SECOND
	}

	@Getter
	@RequiredArgsConstructor
	enum TestJsonValueEnum {
		IN_PROGRESS("In progress"),
		DONE("Done");

		@JsonValue
		private final String value;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	static class TestDrilldownDto extends DataResponseDTO {

		private String input;

		private String text;

		private String fileUpload;

		private String pickList;

		private String inlinePickList;

		private String multifield;

		private String suggestionPickList;

		private TestDictionary dictionary;

		private TestEnum dictionaryEnum;

		private TestJsonValueEnum dictionaryJsonValueEnum;

		private TestEnum radio;

		private Boolean checkbox;

		private Long number;

		private Long percent;

		private BigDecimal money;

		private LocalDateTime date;

		private LocalDateTime dateTime;

		private MultivalueField multiValue;

		private MultivalueField multivalueHover;

		private MultivalueField multipleSelect;

	}

}
