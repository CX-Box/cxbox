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
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.PlatformRequest;
import org.cxbox.dto.ScreenResponsibility;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.validation.MetaValidator.LoadContext;
import org.cxbox.meta.validation.MetaValidator.RequestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

class MetaValidationServiceTest {

	private static final LoadContext LOAD_CONTEXT = new LoadContext(List.of(), List.of(), Map.of());

	private final MetaValidator validator = mock(MetaValidator.class);

	private final MetaValidator other = mock(MetaValidator.class);

	private final MetaRepository metaRepository = mock(MetaRepository.class);

	private final PlatformRequest platformRequest = mock(PlatformRequest.class);

	private final MetaConfigurationProperties config = new MetaConfigurationProperties();

	/**
	 * Parent of service and validator loggers: mocks of {@link MetaValidator} are generated in its package
	 */
	private final Logger logger = (Logger) LoggerFactory.getLogger(MetaValidator.class.getPackageName());

	private final ListAppender<ILoggingEvent> logs = new ListAppender<>();

	@BeforeEach
	void setUp() {
		when(platformRequest.getCrudmaActionType()).thenReturn(CrudmaActionType.META);
		logs.start();
		logger.addAppender(logs);
	}

	@AfterEach
	void detachLogs() {
		logger.detachAppender(logs);
	}

	@Test
	void disabledValidationSkipsValidatorsAndMeta() {
		var service = service();

		validateOnLoad(service);
		service.validateOnRequest("screen", List.of("view"), mock(BcIdentifier.class));

		verifyNoInteractions(validator, other, metaRepository, platformRequest);
		assertThat(logs.list).isEmpty();
	}

	@Test
	void allValidatorsAreEnabledByDefault() {
		config.getValidation().setEnabled(true);

		validateOnLoad(service());

		verify(validator).validateOnLoad(LOAD_CONTEXT);
		verify(other).validateOnLoad(LOAD_CONTEXT);
		assertThat(serviceLogs()).extracting(ILoggingEvent::getLevel, ILoggingEvent::getFormattedMessage)
				.containsExactly(tuple(Level.INFO, "Meta validators enabled: validator, other"));
	}

	@Test
	void emptyIncludeEnablesAllValidators() {
		config.getValidation().setEnabled(true);
		config.getValidation().setInclude(List.of());

		validateOnLoad(service());

		verify(validator).validateOnLoad(LOAD_CONTEXT);
		verify(other).validateOnLoad(LOAD_CONTEXT);
	}

	@Test
	void onlyIncludedValidatorsAreEnabled() {
		config.getValidation().setEnabled(true);
		config.getValidation().setInclude(List.of("other"));

		validateOnLoad(service());

		verify(other).validateOnLoad(LOAD_CONTEXT);
		verifyNoInteractions(validator);
		assertThat(serviceLogs()).extracting(ILoggingEvent::getFormattedMessage)
				.containsExactly("Meta validators enabled: other");
	}

	@Test
	void unknownIncludedValidatorIsReported() {
		config.getValidation().setEnabled(true);
		config.getValidation().setInclude(List.of("othr"));

		validateOnLoad(service());

		verifyNoInteractions(validator, other);
		assertThat(serviceLogs()).extracting(ILoggingEvent::getLevel, ILoggingEvent::getFormattedMessage)
				.containsExactly(tuple(
						Level.WARN,
						"Meta validator \"othr\" from cxbox.meta.validation.include is not found. Available: validator, other"
				));
	}

	@Test
	void notRowMetaRequestSkipsRequestValidators() {
		config.getValidation().setEnabled(true);
		when(platformRequest.getCrudmaActionType()).thenReturn(CrudmaActionType.FIND);

		service().validateOnRequest("screen", List.of("view"), mock(BcIdentifier.class));

		verifyNoInteractions(validator, metaRepository);
	}

	@Test
	void messagesAreLoggedOnEachCall() {
		config.getValidation().setEnabled(true);
		config.getValidation().setInclude(List.of("validator"));
		var screen = new ScreenDTO();
		screen.setViews(List.of(new ViewDTO().setName("view")));
		when(metaRepository.getAllScreens()).thenReturn(Map.of("screen", new ScreenResponsibility().setMeta(screen)));
		when(validator.validateOnLoad(any())).thenReturn(List.of("load"));
		when(validator.validateOnRequest(any())).thenReturn(List.of("request"));
		var service = service();
		var bc = mock(BcIdentifier.class);

		validateOnLoad(service);
		service.validateOnRequest("screen", List.of("view"), bc);
		service.validateOnRequest("screen", List.of("view"), bc);

		assertThat(validatorLogs()).extracting(ILoggingEvent::getFormattedMessage)
				.containsExactly("load", "request", "request");
		assertThat(validatorLogs()).extracting(ILoggingEvent::getLevel).containsOnly(Level.WARN);
		assertThat(validatorLogs()).extracting(ILoggingEvent::getLoggerName)
				.containsOnly(validator.getClass().getName());
	}

	@Test
	void failedValidatorDoesNotBreakOthers() {
		config.getValidation().setEnabled(true);
		when(validator.validateOnLoad(any())).thenThrow(new IllegalStateException());
		when(other.validateOnLoad(any())).thenReturn(List.of("message"));

		assertThatNoException().isThrownBy(() -> validateOnLoad(service()));
		verify(other).validateOnLoad(LOAD_CONTEXT);
		assertThat(validatorLogs()).extracting(ILoggingEvent::getLoggerName, ILoggingEvent::getFormattedMessage)
				.containsExactly(
						tuple(validator.getClass().getName(), "Meta validator failed"),
						tuple(other.getClass().getName(), "message")
				);
		assertThat(validatorLogs().get(0).getThrowableProxy()).isNotNull();
	}

	@Test
	void unknownScreenSkipsRequestValidators() {
		config.getValidation().setEnabled(true);
		when(metaRepository.getAllScreens()).thenReturn(Map.of());

		service().validateOnRequest("screen", List.of("view"), mock(BcIdentifier.class));

		verify(validator, never()).validateOnRequest(any());
	}

	@Test
	void requestContextContainsOnlyAvailableViews() {
		config.getValidation().setEnabled(true);
		var available = new ViewDTO().setName("available");
		var screen = new ScreenDTO();
		screen.setViews(List.of(available, new ViewDTO().setName("forbidden")));
		when(metaRepository.getAllScreens()).thenReturn(Map.of("screen", new ScreenResponsibility().setMeta(screen)));
		var bc = mock(BcIdentifier.class);

		service().validateOnRequest("screen", List.of("available"), bc);

		var context = ArgumentCaptor.forClass(RequestContext.class);
		verify(validator).validateOnRequest(context.capture());
		assertThat(context.getValue().bc()).isSameAs(bc);
		assertThat(context.getValue().screen()).isSameAs(screen);
		assertThat(context.getValue().viewNames()).containsExactly("available");
		assertThat(context.getValue().views()).containsExactly(available);
	}

	/**
	 * Service with validator beans "validator" and "other"
	 */
	private MetaValidationService service() {
		var validators = new LinkedHashMap<String, MetaValidator>();
		validators.put("validator", validator);
		validators.put("other", other);
		return new MetaValidationService(validators, config, metaRepository, platformRequest);
	}

	private static void validateOnLoad(MetaValidationService service) {
		service.validateOnLoad(LOAD_CONTEXT.screens(), LOAD_CONTEXT.views(), LOAD_CONTEXT.widgets());
	}

	private List<ILoggingEvent> serviceLogs() {
		return logs.list.stream()
				.filter(event -> MetaValidationService.class.getName().equals(event.getLoggerName()))
				.toList();
	}

	private List<ILoggingEvent> validatorLogs() {
		return logs.list.stream()
				.filter(event -> !MetaValidationService.class.getName().equals(event.getLoggerName()))
				.toList();
	}

}
