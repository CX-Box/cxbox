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

import java.util.Locale;
import org.cxbox.api.service.LocaleService;
import org.cxbox.api.service.session.CoreSessionService;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

public class EnhancedLocaleResolver extends CookieLocaleResolver {

	private final CoreSessionService coreSessionService;

	private final LocaleService localeService;

	public EnhancedLocaleResolver(CoreSessionService coreSessionService, LocaleService localeService) {
		super("locale");
		this.coreSessionService = coreSessionService;
		this.localeService = localeService;
		setRejectInvalidCookies(false);
		setLanguageTagCompliant(false);

		setDefaultLocaleFunction(req -> {
			Locale fallback = getDefaultLocale() != null ? getDefaultLocale() : req.getLocale();
			Locale locale = coreSessionService.getLocale(fallback);
			return localeService.isLanguageSupported(locale.getLanguage()) ? locale : localeService.getDefaultLocale();
		});

		setDefaultTimeZoneFunction(req -> coreSessionService.getTimeZone(getDefaultTimeZone()));
	}

	@Override
	public Locale parseLocaleValue(String localeValue) {
		Locale locale = super.parseLocaleValue(localeValue);
		if (locale == null || !localeService.isLanguageSupported(locale.getLanguage())) {
			return null;
		}
		return locale;
	}


}
