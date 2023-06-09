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

package org.cxbox.core.util.session.impl;

import org.cxbox.api.data.dictionary.CoreDictionaries.SystemPref;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.system.SystemSettings;
import org.cxbox.core.dto.LoggedUser;
import org.cxbox.core.dto.data.view.ScreenResponsibility;
import org.cxbox.core.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.core.service.ScreenResponsibilityService;
import org.cxbox.core.service.UIService;
import org.cxbox.core.service.impl.UserRoleService;
import org.cxbox.core.util.session.LoginService;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.model.core.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

	private final SessionService sessionService;

	private final UserRoleService userRoleService;

	private final SystemSettings systemSettings;

	private final UIService uiService;

	private final ScreenResponsibilityService screenResponsibilityService;

	private final MetaConfigurationProperties metaConfigurationProperties;

	/**
	 * Build info for active session user for specific role
	 *
	 * @param role Requested role
	 * @return LoggedUser User info with settings, supported features, locale and time zone
	 */
	@Override
	public LoggedUser getLoggedUser(String role) {

		sessionService.setSessionUserInternalRole(role);

		User user = sessionService.getSessionUser();
		LOV activeUserRole = sessionService.getSessionUserRole();

		return LoggedUser.builder()
				.sessionId(sessionService.getSessionId())
				.user(user)
				.activeRole(activeUserRole.getKey())
				.roles(userRoleService.getUserRoles(user))
				// TODO: Remove screens from response in 3.0 in favor of separate ScreenController endpoint
				.screens(getScreens(user, activeUserRole))
				.userSettings(uiService.getUserSettings())
				.featureSettings(this.getFeatureSettings())
				.systemUrl(systemSettings.getValue(SystemPref.SYSTEM_URL))
				.language(LocaleContextHolder.getLocale().getLanguage())
				.timezone(LocaleContextHolder.getTimeZone().getID())
				.devPanelEnabled(metaConfigurationProperties.isDevPanelEnabled())
				.build();
	}

	/**
	 * Get all available screens with respect of user role
	 * @deprecated TODO: Remove in 3.0 in favor of separate ScreenController endpoint
	 *
	 * @param user Active session user
	 * @param userRole User role
	 * @return JsonNode Available screens
	 */
	@Deprecated
	private List<ScreenResponsibility> getScreens(User user, LOV userRole) {
		return screenResponsibilityService.getScreens(user, userRole);
	}

	/**
	 * Get available application features, e.g. comments/notification polling or supression of system errors
	 * No implementation is provided by Cxbox UI by default so for now it is considered as a customization joint.
	 *
	 * @return Dictionary of string key and value (boolean)
	 * Following keys were supported historically: FEATURE_COMMENTS, FEATURE_NOTIFICATIONS, FEATURE_HIDE_SYSTEM_ERRORS
	 */
	public Collection<SimpleDictionary> getFeatureSettings() {
		return systemSettings.select(key -> key.startsWith("FEATURE_"))
				.map(p -> new SimpleDictionary(p.getKey(), p.getValue()))
				.collect(Collectors.toList());
	}
}
