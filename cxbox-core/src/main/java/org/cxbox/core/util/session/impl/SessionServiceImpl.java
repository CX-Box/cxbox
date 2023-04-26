/*-
 * #%L
 * IO Cxbox - Core
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.core.util.session.impl;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.CoreSessionService;
import org.cxbox.api.service.session.CxboxUserDetailsInterface;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.core.controller.BcHierarchyAware;
import org.cxbox.core.service.UIService;
import org.cxbox.core.service.impl.UserRoleService;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.core.util.session.UserExternalService;
import org.cxbox.core.util.session.UserService;
import org.cxbox.core.util.session.WebHelper;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.core.entity.Department;
import org.cxbox.model.core.entity.Division;
import org.cxbox.model.core.entity.User;
import org.cxbox.model.core.entity.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Вспомогательный класс для получения данных о текущем пользователе (имя, логин, роли и т.п)
 */
@Slf4j
@Service("sessionService")
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

	private final UIService uiService;

	private final Optional<List<UserExternalService>> userExternalServices;

	private final UserService userService;

	private final UserRoleService userRoleService;

	private final JpaDao jpaDao;

	private final CoreSessionService coreSessionService;

	private final BcHierarchyAware bcHierarchyAware;

	private final UserCache userCache;

	// если у нас транзакции нет, то здесь будут происходить
	// постоянные запросы к СУБД, поэтому кешируем
	@Override
	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = {CacheConfig.REQUEST_CACHE}, key = "#root.methodName")
	public User getSessionUser() {
		User user = getUserFromDetails(coreSessionService.getSessionUserDetails(true));
		if (user == null) {
			throw new SessionAuthenticationException("Not authorized");
		}
		return user;
	}

	@Override
	public Department getSessionUserDepartment() {
		return getSessionUser().getDepartment();
	}

	@Override
	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = {CacheConfig.REQUEST_CACHE}, key = "#root.methodName")
	public LOV getSessionUserRole() {
		CxboxUserDetailsInterface userDetails = coreSessionService.getSessionUserDetails(true);
		HttpServletRequest request = WebHelper.getCurrentRequest().orElse(null);
		if (request == null) {
			return userDetails.getUserRole();
		}
		return calculateUserRole(request, userDetails);
	}

	private LOV calculateUserRole(HttpServletRequest request, CxboxUserDetailsInterface userDetails) {
		LOV mainRole = userDetails.getUserRole();
		String requestedRole = request.getHeader("RequestedUserRole");
		// в заголовке ничего не указано - возвращаем main
		if (StringUtils.isBlank(requestedRole)) {
			return mainRole;
		}
		// оптимизация: в заголовке совпадает с сессией - возвращаем main
		if (mainRole != null && requestedRole.equals(mainRole.getKey())) {
			return mainRole;
		}
		LOV currentRole = userRoleService.getMatchedRole(getUserFromDetails(userDetails), requestedRole);
		if (currentRole == null) {
			currentRole = userDetails.getUserRole();
		}
		return currentRole;
	}

	@Override
	public void setSessionUserTimezone(LOV timezone) {
		CxboxUserDetailsInterface userDetails = coreSessionService.getSessionUserDetails(true);
		if (timezone == null || userDetails == null) {
			return;
		}
		userDetails.setTimezone(timezone);
	}

	@Override
	public void setSessionUserLocale(LOV locale) {
		CxboxUserDetailsInterface userDetails = coreSessionService.getSessionUserDetails(true);
		if (locale == null || userDetails == null) {
			return;
		}
		userDetails.setLocaleCd(locale);
	}

	@Override
	public void setSessionUserInternalRole(String role) {
		CxboxUserDetailsInterface userDetails = coreSessionService.getSessionUserDetails(true);
		if (role == null || role.isEmpty() || userDetails == null) {
			return;
		}
		User user = getUserFromDetails(userDetails);
		LOV matchedRole = userRoleService.getMatchedRole(user, role);
		if (matchedRole == null) {
			return;
		}
		userDetails.setUserRole(matchedRole);
		userRoleService.updateMainUserRole(user, matchedRole);
	}

	@Override
	public String getSessionIpAddress() {
		String forwardedFor = "X-Forwarded-For";
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		try {
			if (attrs.getRequest().getHeader(forwardedFor) == null
					|| attrs.getRequest().getHeader(forwardedFor).trim().length() == 0) {
				return attrs.getRequest().getRemoteAddr();
			} else {
				return attrs.getRequest().getHeader(forwardedFor);
			}
		} catch (Exception e) {
			log.warn("Cannot get user ip", e);
			return "";
		}
	}

	@Override
	public Map<String, Boolean> getResponsibilities() {
		return userCache.getResponsibilities(getSessionUser(), getSessionUserRole());
	}

	@Override
	public String getFirstViewFromResponsibilities(String... views) {
		return uiService.getFirstViewFromResponsibilities(getSessionUser(), getSessionUserRole(), views);
	}

	private User getUserFromDetails(final CxboxUserDetailsInterface userDetails) {
		return jpaDao.findById(User.class, userDetails.getId());
	}

	@Override
	public String getSessionId() {
		return coreSessionService.getSessionId();
	}

	@Override
	public Division getSessionUserDivision(LOV levelCd) {
		return getSessionUserRoles().stream()
				.filter(userRole -> Objects.equals(getSessionUserRole(), userRole.getInternalRoleCd()))
				.findFirst()
				.map(UserRole::getDivision)
				.map(division -> division.getParentByLevelCode(levelCd)).orElse(null);
	}

	@Override
	public Division getSessionUserDivision() {
		return getSessionUserRoles().stream()
				.filter(userRole -> Objects.equals(getSessionUserRole(), userRole.getInternalRoleCd()))
				.findFirst()
				.map(UserRole::getDivision)
				.orElse(null);
	}

	@Override
	public List<UserRole> getSessionUserRoles() {
		return getSessionUser().getUserRoleList();
	}

	/**
	 * Возвращает доступные вью текущего скрина
	 */
	@Override
	public Collection<String> getCurrentScreenViews() {
		return getViews(bcHierarchyAware.getScreenName());
	}

	@Override
	public List<String> getViews(final String screenName) {
		return userCache.getViews(screenName, getSessionUser(), getSessionUserRole());
	}



	@Component
	@RequiredArgsConstructor
	public static class UserCache {

		private final UIService uiService;

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = {CacheConfig.USER_CACHE},
				key = "{#root.methodName, #user.id, #userRole}"
		)
		public Map<String, Boolean> getResponsibilities(final User user, final LOV userRole) {
			return uiService.getResponsibilities(
					user,
					userRole
			);
		}

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = {CacheConfig.USER_CACHE},
				key = "{#root.methodName, #screenName, #user.id, #userRole}"
		)
		public List<String> getViews(final String screenName, final User user, final LOV userRole) {
			return uiService.getViews(
					screenName,
					user,
					userRole
			);
		}

	}

}
