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

import org.cxbox.api.service.LocaleService;
import org.cxbox.api.service.session.CoreSessionService;
import java.util.Locale;
import java.util.TimeZone;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

public class EnhancedLocaleResolver extends CookieLocaleResolver {

	private final CoreSessionService coreSessionService;

	private final LocaleService localeService;

	public EnhancedLocaleResolver(CoreSessionService coreSessionService, LocaleService localeService) {
		this.coreSessionService = coreSessionService;
		this.localeService = localeService;
		setRejectInvalidCookies(false);
		setLanguageTagCompliant(false);
		setCookieName("locale");
	}

	@Override
	public Locale parseLocaleValue(String localeValue) {
		Locale locale = super.parseLocaleValue(localeValue);
		if (locale == null || !localeService.isLanguageSupported(locale.getLanguage())) {
			return null;
		}
		return locale;
	}

	@Override
	public Locale determineDefaultLocale(HttpServletRequest request) {
		Locale locale = coreSessionService.getLocale(super.determineDefaultLocale(request));
		if (localeService.isLanguageSupported(locale.getLanguage())) {
			return locale;
		}
		return localeService.getDefaultLocale();
	}

	@Override
	public TimeZone determineDefaultTimeZone(HttpServletRequest request) {
		return coreSessionService.getTimeZone(super.determineDefaultTimeZone(request));
	}

}
