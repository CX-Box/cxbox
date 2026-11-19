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

package org.cxbox.meta.metahotreload.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cxbox.api.MetaHotReloadService;
import org.cxbox.api.service.session.InternalAuthorizationService;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.dto.MetaValidationResult;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.validation.MetaValidationService;
import org.cxbox.meta.validation.MetaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class MetaHotReloadServiceImplTest {

	private final MetaConfigurationProperties config = new MetaConfigurationProperties();

	private final MetaResourceReaderService reader = mock(MetaResourceReaderService.class);

	private final TransactionService txService = mock(TransactionService.class);

	private final MetaValidator validator = mock(MetaValidator.class);

	private final AtomicBoolean inNoTxCallback = new AtomicBoolean();

	@BeforeEach
	void setUp() {
		config.setDevPanelEnabled(true);
		when(reader.getScreens()).thenReturn(List.of());
		when(reader.getViews()).thenReturn(List.of());
		when(reader.getWidgets()).thenReturn(List.of());
		when(txService.invokeNoTx(any())).thenAnswer(invocation -> {
			inNoTxCallback.set(true);
			try {
				return invocation.<Invoker<?, ?>>getArgument(0).invoke();
			} finally {
				inNoTxCallback.set(false);
			}
		});
	}

	@Test
	void validatorsUseNoTxCallback() {
		config.getValidation().setEnabled(true);
		var runs = new ArrayList<Boolean>();
		when(validator.validate(any(), any(), any())).thenAnswer(invocation -> {
			runs.add(inNoTxCallback.get());
			return List.of();
		});
		var service = service();

		service.loadMeta();
		service.validateMeta();

		assertThat(runs).containsExactly(true, true);
	}

	@Test
	void defaultValidationReturnsEmptyList() {
		MetaHotReloadService service = () -> {
		};

		assertThat(service.validateMeta()).isEmpty();
	}

	@Test
	void disabledValidationGivesNoResult() {
		var service = service();

		service.loadMeta();
		assertThat(service.validateMeta()).isEmpty();
		verifyNoInteractions(validator);
	}

	@Test
	void disabledDevPanelGivesNoResult() {
		config.setDevPanelEnabled(false);
		config.getValidation().setEnabled(true);
		var service = service();

		assertThat(service.validateMeta()).isEmpty();
		verifyNoInteractions(validator);

		service.loadMeta();
		verify(validator).validate(any(), any(), any());
	}

	@Test
	void problemsAreLoggedOnLoadOnly() {
		config.getValidation().setEnabled(true);
		when(validator.validate(any(), any(), any())).thenReturn(List.of(MetaValidationResult.of("first")));
		var service = service();
		var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		var logs = new ListAppender<ILoggingEvent>();
		logs.start();
		logger.addAppender(logs);
		try {
			service.validateMeta();
			assertThat(logs.list).isEmpty();
			service.loadMeta();
		} finally {
			logger.detachAppender(logs);
		}

		assertThat(logs.list).extracting(ILoggingEvent::getLevel, ILoggingEvent::getFormattedMessage)
				.contains(tuple(Level.ERROR, "[META_VALIDATION] validator: 1 problem(s)\n- first"));
	}

	@Test
	void validationReadsMetaOnEachCall() {
		config.getValidation().setEnabled(true);
		when(validator.validate(any(), any(), any())).thenReturn(List.of(MetaValidationResult.of("first")))
				.thenReturn(List.of());
		var service = service();

		assertThat(service.validateMeta()).extracting(
						MetaValidationResult::getValidatorName,
						MetaValidationResult::getMessage
				)
				.containsExactly(tuple("validator", "first"));
		assertThat(service.validateMeta()).isEmpty();
		verify(reader, times(2)).getWidgets();
	}

	@Test
	void unreadableMetaGivesFailedMessage() {
		config.getValidation().setEnabled(true);
		when(reader.getWidgets()).thenAnswer(invocation -> {
			throw new JsonParseException(null, "broken json");
		});

		assertThat(service().validateMeta())
				.extracting(
						MetaValidationResult::getValidatorName,
						MetaValidationResult::getMessage,
						MetaValidationResult::isExecutionFailed
				)
				.containsExactly(tuple(
						"metaValidation",
						"Meta validation failed: com.fasterxml.jackson.core.JsonParseException: broken json",
						true
				));
		verifyNoInteractions(validator);
	}

	@Test
	void failedValidatorDoesNotBreakLoad() {
		config.getValidation().setEnabled(true);
		when(validator.validate(any(), any(), any())).thenAnswer(invocation -> {
			throw new IOException("validator failed");
		});

		service().loadMeta();

		verify(validator).validate(any(), any(), any());
		verify(txService).invokeInTx(any());
	}

	@Test
	void sameCollectionsAreValidatedOnLoadAndForDevPanel() {
		config.getValidation().setEnabled(true);
		var screens = List.of(new ScreenSourceDto());
		var views = List.of(new ViewSourceDTO());
		var widgets = Map.<WidgetSourceDTO, String>of();
		when(reader.getScreens()).thenReturn(screens);
		when(reader.getViews()).thenReturn(views);
		var service = service();

		service.loadMeta();
		service.validateMeta();

		verify(validator, times(2)).validate(screens, views, widgets);
	}

	@Test
	@SuppressWarnings("removal")
	void deprecatedConstructorRunsNoValidators() {
		config.getValidation().setEnabled(true);
		var service = new MetaHotReloadServiceImpl(
				config, reader, mock(InternalAuthorizationService.class), txService,
				mock(MetaRepository.class), Optional.empty()
		);

		assertThat(service.validateMeta()).isEmpty();
	}

	private MetaHotReloadServiceImpl service() {
		return new MetaHotReloadServiceImpl(
				config,
				reader,
				mock(InternalAuthorizationService.class),
				txService,
				mock(MetaRepository.class),
				Optional.empty(),
				new MetaValidationService(Map.of("validator", validator), txService)
		);
	}

}
