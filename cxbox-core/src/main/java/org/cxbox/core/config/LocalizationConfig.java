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

package org.cxbox.core.config;

import lombok.AllArgsConstructor;
import org.cxbox.api.config.CxboxLocalizationProperties;
import org.cxbox.api.service.LocaleService;
import org.cxbox.api.service.session.CoreSessionService;
import org.cxbox.model.core.api.TranslationService;
import org.cxbox.model.core.service.LocaleServiceImpl;
import org.cxbox.model.core.service.TranslationServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

@AllArgsConstructor
@EnableConfigurationProperties(CxboxLocalizationProperties.class)
public class LocalizationConfig {

	@Bean(name = DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
	public LocaleResolver localeResolver(CoreSessionService coreSessionService, LocaleService localeService) {
		return new EnhancedLocaleResolver(coreSessionService, localeService);
	}

	@Bean(name = LocaleService.SERVICE_NAME)
	public LocaleService localeService(CxboxLocalizationProperties cxboxLocalizationProperties) {
		return new LocaleServiceImpl(cxboxLocalizationProperties);
	}

	@Bean
	public TranslationService translationService(LocaleService localeService) {
		return new TranslationServiceImpl(localeService);
	}



}
