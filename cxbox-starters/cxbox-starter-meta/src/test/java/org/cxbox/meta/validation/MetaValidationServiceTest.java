/*
 * © OOO "SI IKS LAB", 2022-2026
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

package org.cxbox.meta.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.core.dto.LoggedUser;
import org.cxbox.dto.MetaValidationResult;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO.ViewWidgetSourceDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;

class MetaValidationServiceTest {

	private static final LogLevel LOG_LEVEL = LogLevel.ERROR;

	private final MetaValidator validator = mock(MetaValidator.class);

	private final MetaValidator other = mock(MetaValidator.class);

	private List<ViewSourceDTO> views = List.of();

	/**
	 * Captures validator logs because Mockito generates validator classes in this package.
	 */
	private final Logger validationLogger = (Logger) LoggerFactory.getLogger(MetaValidator.class.getPackageName());

	private final ListAppender<ILoggingEvent> validationLogs = new ListAppender<>();

	@BeforeEach
	void setUp() {
		validationLogs.start();
		validationLogger.addAppender(validationLogs);
	}

	@AfterEach
	void detachLogs() {
		validationLogger.detachAppender(validationLogs);
	}

	@Test
	void messagesAreReturnedAndLoggedWithErrorLevel() {
		when(validator.validate(any(), any(), any())).thenReturn(List.of(
				MetaValidationResult.of("first"),
				MetaValidationResult.forWidget("widget", "second").setViews(Set.of("view"))
		));
		when(other.validate(any(), any(), any())).thenReturn(List.of());

		var result = runValidators(validators(), views, LOG_LEVEL);

		assertThat(result)
				.extracting(
						MetaValidationResult::getValidatorName,
						MetaValidationResult::getMessage,
						MetaValidationResult::getViews,
						MetaValidationResult::getWidgets
				)
				.containsExactly(
						tuple("validator", "first", Set.of(), Set.of()),
						tuple("validator", "second", Set.of("view"), Set.of("widget"))
				);
		assertThat(validationLogs.list).extracting(ILoggingEvent::getFormattedMessage)
				.containsExactly("[META_VALIDATION] validator: 2 problem(s)\n- first\nview:\n  - widget: second");
		assertThat(validationLogs.list).extracting(ILoggingEvent::getLevel).containsOnly(Level.ERROR);
		assertThat(validationLogs.list).extracting(ILoggingEvent::getLoggerName)
				.containsOnly(validator.getClass().getName());
	}

	@Test
	void problemsAreLoggedWithConfiguredLevel() {
		when(validator.validate(any(), any(), any())).thenReturn(List.of(MetaValidationResult.of("first")));
		when(other.validate(any(), any(), any())).thenReturn(List.of());

		runValidators(validators(), views, LogLevel.WARN);

		assertThat(validationLogs.list).extracting(ILoggingEvent::getLevel).containsExactly(Level.WARN);
	}

	@Test
	void problemOfWidgetWithoutViewsIsShownOnAllViewsOfWidget() {
		views = List.of(view("first", "widget"), view("second", "other", "widget"), view("third", "other"));
		when(validator.validate(any(), any(), any())).thenReturn(List.of(
				MetaValidationResult.forWidget("widget", "without views"),
				MetaValidationResult.forWidget("widget", "with views").setViews(Set.of("third"))
		));
		when(other.validate(any(), any(), any())).thenReturn(List.of());

		var result = runValidators(validators(), views, LOG_LEVEL);

		assertThat(result).extracting(MetaValidationResult::getMessage, MetaValidationResult::getViews).containsExactly(
				tuple("without views", Set.of("first", "second")),
				tuple("with views", Set.of("third"))
		);
	}

	@Test
	void problemsOfValidatorAreLoggedAsOneRecordGroupedByViews() {
		views = List.of(view("first", "widget", "other"), view("second", "widget"));
		when(validator.validate(any(), any(), any())).thenReturn(List.of(
				MetaValidationResult.forWidget("widget", "of widget").setServerLogDetails("details"),
				MetaValidationResult.forWidget("other", "of other widget").setServerLogDetails("details"),
				MetaValidationResult.forWidget("unknown", "of widget not on view"),
				MetaValidationResult.forWidget("widget", "on second\nsecond line").setViews(Set.of("second")),
				MetaValidationResult.of("not bound").setServerLogDetails("other details")
		));
		when(other.validate(any(), any(), any())).thenReturn(List.of());

		var result = runValidators(validators(), views, LOG_LEVEL);

		var expected = String.join(
				"\n",
				"[META_VALIDATION] validator: 5 problem(s)",
				"- not bound",
				"first, second:",
				"  - widget: of widget",
				"first:",
				"  - other: of other widget",
				"second:",
				"  - widget: on second",
				"    second line",
				"not on any view:",
				"  - unknown: of widget not on view",
				"details",
				"other details"
		);
		assertThat(validationLogs.list).extracting(ILoggingEvent::getFormattedMessage).containsExactly(expected);
		assertThat(result).extracting(MetaValidationResult::getMessage).containsExactly(
				"of widget", "of other widget", "of widget not on view", "on second\nsecond line", "not bound"
		);
	}

	@Test
	void problemOfViewIsShownOnThisView() {
		views = List.of(view("first", "widget"));
		when(validator.validate(any(), any(), any())).thenReturn(List.of(
				MetaValidationResult.of("of views").setViews(Set.of("first", "second")),
				MetaValidationResult.forWidget("widget", "of widget")
		));

		var result = runValidators(Map.of("validator", validator), views, LOG_LEVEL);

		assertThat(result).extracting(
						MetaValidationResult::getMessage,
						MetaValidationResult::getViews,
						MetaValidationResult::getWidgets
				)
				.containsExactly(
						tuple("of views", Set.of("first", "second"), Set.of()),
						tuple("of widget", Set.of("first"), Set.of("widget"))
				);
	}

	@Test
	void loginContainsEmptyValidationArrayByDefault() throws Exception {
		var mapper = new ObjectMapper();
		var json = mapper.readTree(mapper.writeValueAsString(LoggedUser.builder().build()));

		assertThat(json.path("metaValidation").isArray()).isTrue();
		assertThat(json.path("metaValidation").size()).isZero();
	}

	@Test
	void messagesExposeExecutionStatusAndHideLogDetails() throws Exception {
		var message = MetaValidationResult.forWidget("widget", "invalid type").setViews(Set.of("view"))
				.setValidatorName("validator").setServerLogDetails("server details");
		var mapper = new ObjectMapper();

		var json = mapper.readTree(mapper.writeValueAsString(List.of(message)));

		assertThat(json.isArray()).isTrue();
		assertThat(json.get(0).has("serverLogDetails")).isFalse();
		assertThat(json.get(0).path("validatorName").asText()).isEqualTo("validator");
		assertThat(json.get(0).has("validator")).isFalse();
		assertThat(json.get(0).path("executionFailed").isBoolean()).isTrue();
		assertThat(json.get(0).path("executionFailed").asBoolean()).isFalse();
		assertThat(json.get(0).has("failed")).isFalse();
		assertThat(json.get(0).path("message").asText()).isEqualTo("invalid type");
		assertThat(json.get(0).path("widgets").get(0).asText()).isEqualTo("widget");
	}

	@Test
	void noValidatorsGiveEmptyResult() {
		var result = runValidators(Map.of(), views, LOG_LEVEL);

		assertThat(result).isEmpty();
	}

	@Test
	void failedValidatorDoesNotBreakOthers() {
		when(validator.validate(any(), any(), any())).thenThrow(new IllegalStateException("broken"));
		when(other.validate(any(), any(), any())).thenReturn(List.of(MetaValidationResult.of("message")));

		assertThatNoException().isThrownBy(() -> {
			var result = runValidators(validators(), views, LOG_LEVEL);
			assertThat(result).extracting(
							MetaValidationResult::getValidatorName,
							MetaValidationResult::getMessage,
							MetaValidationResult::isExecutionFailed
					)
					.containsExactly(
							tuple("validator", "Meta validator failed: java.lang.IllegalStateException: broken", true),
							tuple("other", "message", false)
					);
		});
		verify(other).validate(any(), any(), any());
		assertThat(validationLogs.list).extracting(ILoggingEvent::getLoggerName, ILoggingEvent::getFormattedMessage)
				.containsExactly(
						tuple(
								validator.getClass().getName(),
								"[META_VALIDATION] Meta validator validator failed, its problems are unknown"
						),
						tuple(other.getClass().getName(), "[META_VALIDATION] other: 1 problem(s)\n- message")
				);
		assertThat(validationLogs.list.get(0).getThrowableProxy()).isNotNull();
	}

	@Test
	void onlyFailureIsLoggedWithLevelOff() {
		when(validator.validate(any(), any(), any())).thenReturn(List.of(MetaValidationResult.of("first")));
		when(other.validate(any(), any(), any())).thenThrow(new IllegalStateException("broken"));

		var result = runValidators(validators(), views, LogLevel.OFF);

		assertThat(result).extracting(MetaValidationResult::getMessage)
				.containsExactly("first", "Meta validator failed: java.lang.IllegalStateException: broken");
		assertThat(validationLogs.list).extracting(ILoggingEvent::getLevel, ILoggingEvent::getFormattedMessage)
				.containsExactly(tuple(Level.WARN, "[META_VALIDATION] Meta validator other failed, its problems are unknown"));
	}

	@Test
	void logRecordsStartWithCommonPrefix() {
		when(validator.validate(any(), any(), any())).thenThrow(new IllegalStateException("broken"));
		when(other.validate(any(), any(), any())).thenReturn(List.of(MetaValidationResult.of("message")));

		runValidators(validators(), views, LOG_LEVEL);

		assertThat(validationLogs.list).hasSize(2).extracting(ILoggingEvent::getFormattedMessage)
				.allMatch(message -> message.startsWith("[META_VALIDATION] "));
	}

	@Test
	void titleAndTitleFailureAreReported() {
		when(validator.validate(any(), any(), any())).thenReturn(List.of(MetaValidationResult.of("first")));
		when(validator.title()).thenReturn("Validator title");
		when(other.validate(any(), any(), any())).thenReturn(List.of(MetaValidationResult.of("second")));
		when(other.title()).thenThrow(new IllegalStateException("broken title"));

		var result = runValidators(validators(), views, LOG_LEVEL);

		assertThat(result)
				.extracting(
						MetaValidationResult::getValidatorName,
						MetaValidationResult::getTitle,
						MetaValidationResult::getMessage,
						MetaValidationResult::isExecutionFailed
				)
				.containsExactly(
						tuple("validator", "Validator title", "first", false),
						tuple(
								"other", "other",
								"Meta validator failed: java.lang.IllegalStateException: broken title", true
						)
				);
	}

	@Test
	void blankTitleIsShownAsBeanName() {
		when(validator.validate(any(), any(), any())).thenReturn(List.of(MetaValidationResult.of("first")));
		when(validator.title()).thenReturn("   ");

		var result = runValidators(Map.of("validator", validator), views, LOG_LEVEL);

		assertThat(result).extracting(MetaValidationResult::getTitle).containsExactly("validator");
		assertThat(validationLogs.list).extracting(ILoggingEvent::getFormattedMessage)
				.containsExactly("[META_VALIDATION] validator: 1 problem(s)\n- first");
	}

	@Test
	void nullProblemsAreIgnored() {
		var problems = new ArrayList<MetaValidationResult>();
		problems.add(null);
		problems.add(new MetaValidationResult().setViews(new HashSet<>(Arrays.asList("view", null))));
		when(validator.validate(any(), any(), any())).thenReturn(null);
		when(other.validate(any(), any(), any())).thenReturn(problems);

		var result = runValidators(validators(), views, LOG_LEVEL);

		assertThat(result).extracting(
						MetaValidationResult::getValidatorName,
						MetaValidationResult::getMessage,
						MetaValidationResult::getViews
				)
				.containsExactly(tuple("other", "", Set.of("view")));
		assertThat(validationLogs.list).extracting(ILoggingEvent::getFormattedMessage)
				.containsExactly("[META_VALIDATION] other: 1 problem(s)\nview:\n  - ");
	}

	@Test
	void problemsAreNotLimited() {
		var longMessage = "x".repeat(10_000);
		var problems = IntStream.range(0, 1_000)
				.mapToObj(i -> MetaValidationResult.of(i == 0 ? longMessage : "problem " + i))
				.toList();
		when(validator.validate(any(), any(), any())).thenReturn(problems);

		var result = runValidators(Map.of("validator", validator), views, LOG_LEVEL);

		assertThat(result).extracting(MetaValidationResult::getMessage)
				.hasSize(1_000)
				.startsWith(longMessage)
				.endsWith("problem 999");
	}

	@Test
	void checkedExceptionOfValidatorIsReported() {
		when(validator.validate(any(), any(), any())).thenAnswer(invocation -> {
			throw new IOException("unreadable file");
		});

		var result = runValidators(Map.of("validator", validator), views, LOG_LEVEL);

		assertThat(result).singleElement().extracting(MetaValidationResult::getMessage)
				.isEqualTo("Meta validator failed: java.io.IOException: unreadable file");
	}

	@Test
	void errorOfValidatorIsNotCaught() {
		when(validator.validate(any(), any(), any())).thenThrow(new NoClassDefFoundError("optional/Dependency"));

		assertThatThrownBy(() -> runValidators(Map.of("validator", validator), views, LOG_LEVEL))
				.isInstanceOf(NoClassDefFoundError.class);
	}

	private static List<MetaValidationResult> runValidators(Map<String, MetaValidator> validators,
			List<ViewSourceDTO> views,
			LogLevel level) {
		return new MetaValidationService(validators, mock(TransactionService.class)).runValidators(
				List.of(),
				views,
				Map.of(),
				level
		);
	}

	private Map<String, MetaValidator> validators() {
		var validators = new LinkedHashMap<String, MetaValidator>();
		validators.put("validator", validator);
		validators.put("other", other);
		return validators;
	}

	private static ViewSourceDTO view(String name, String... widgetNames) {
		var view = new ViewSourceDTO();
		view.setName(name);
		view.setWidgets(Arrays.stream(widgetNames).map(widgetName -> {
			var widget = new ViewWidgetSourceDTO();
			widget.setWidgetName(widgetName);
			return widget;
		}).toList());
		return view;
	}

}
