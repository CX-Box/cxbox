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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.PlatformRequest;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties.Validation;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.validation.MetaValidator.LoadContext;
import org.cxbox.meta.validation.MetaValidator.RequestContext;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Service;

/**
 * Runs {@link MetaValidator} beans enabled by {@link Validation} and logs their messages
 */
@Slf4j
@Service
public class MetaValidationService {

	/**
	 * Value of {@link Validation#getInclude()} enabling all validators, same as empty list
	 */
	public static final String ALL = "*";

	/**
	 * Enabled validators, empty when validation is disabled
	 */
	private final List<MetaValidator> validators;

	private final MetaRepository metaRepository;

	private final PlatformRequest platformRequest;

	public MetaValidationService(Map<String, MetaValidator> validators, MetaConfigurationProperties config,
			MetaRepository metaRepository, PlatformRequest platformRequest) {
		this.validators = enabledMetaValidator(validators, config.getValidation());
		this.metaRepository = metaRepository;
		this.platformRequest = platformRequest;
	}

	/**
	 * Runs on startup and on meta refresh, context is created only when validation is enabled
	 */
	public void validateOnLoad(List<ScreenSourceDto> screens, List<ViewSourceDTO> views,
			Map<WidgetSourceDTO, String> widgets) {
		if (validators.isEmpty()) {
			return;
		}
		var context = new LoadContext(screens, views, widgets);
		run(validator -> validator.validateOnLoad(context));
	}

	/**
	 * Runs only on row-meta request, so messages are logged once per screen opening
	 */
	public void validateOnRequest(String screenName, Collection<String> viewNames, BcIdentifier bc) {
		if (validators.isEmpty() || platformRequest.getCrudmaActionType() != CrudmaActionType.META) {
			return;
		}
		var screenDto = metaRepository.getAllScreens().get(screenName);
		if (screenDto == null) {
			return;
		}
		var screen = (ScreenDTO) screenDto.getMeta();
		var views = screen.getViews().stream()
				.filter(view -> viewNames.contains(view.getName()))
				.toList();
		var context = new RequestContext(bc, screenName, screen, List.copyOf(viewNames), views);
		run(validator -> validator.validateOnRequest(context));
	}

	private void run(Function<MetaValidator, List<String>> validation) {
		for (MetaValidator validator : validators) {
			var log = LoggerFactory.getLogger(AopUtils.getTargetClass(validator));
			try {
				validation.apply(validator).forEach(log::warn);
			} catch (RuntimeException e) {
				log.warn("Meta validator failed", e);
			}
		}
	}

	private static List<MetaValidator> enabledMetaValidator(Map<String, MetaValidator> validators, Validation config) {
		if (!config.isEnabled()) {
			return List.of();
		}
		var include = config.getInclude();
		var all = include.isEmpty() || include.contains(ALL);
		include.stream()
				.filter(name -> !ALL.equals(name) && !validators.containsKey(name))
				.forEach(name -> log.warn("Meta validator \"" + name + "\" from cxbox.meta.validation.include is not found. "
						+ "Available: " + String.join(", ", validators.keySet())));
		var enabledNames = validators.keySet().stream()
				.filter(name -> all || include.contains(name))
				.toList();
		if (!enabledNames.isEmpty()) {
			log.info("Meta validators enabled: {}", String.join(", ", enabledNames));
		}
		return enabledNames.stream().map(validators::get).toList();
	}

}
