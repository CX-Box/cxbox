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

package org.cxbox.model.core.service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.LocaleUtils;
import org.cxbox.api.config.CxboxLocalizationProperties;
import org.cxbox.api.service.LocaleService;
import org.springframework.context.i18n.LocaleContextHolder;



@RequiredArgsConstructor
public class LocaleServiceImpl implements LocaleService {

	@Getter
	private Set<String> supportedLanguages;

	public LocaleServiceImpl(CxboxLocalizationProperties localizationProperties) {
		this.supportedLanguages = checkLanguages(localizationProperties.getSupportedLanguages());
	}

	@Override
	public boolean isLanguageSupported(String language) {
		return supportedLanguages.contains(language);
	}

	private Set<String> checkLanguages(Set<String> supportedLanguages) {
		if (supportedLanguages.isEmpty()) {
			throw new IllegalStateException("Please specify SUPPORTED_LANGUAGES in system settings");
		}
		LocaleService.defaultLocale.set(LocaleUtils.toLocale(supportedLanguages.stream().findFirst().orElseThrow()));
		LocaleContextHolder.setDefaultLocale(defaultLocale.get());
		return Collections.unmodifiableSet(new LinkedHashSet<>(supportedLanguages));
	}

	@Override
	public Locale getDefaultLocale() {
		return LocaleService.defaultLocale.get();
	}

}
