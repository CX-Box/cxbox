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

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.CoreSessionService;
import org.cxbox.api.service.session.CxboxUserDetailsInterface;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.core.controller.BcHierarchyAware;
import org.cxbox.core.service.UIService;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.core.util.session.WebHelper;
import org.cxbox.api.service.session.IUser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Вспомогательный класс для получения данных о текущем пользователе (имя, логин, роли и т.п)
 */
@Slf4j
@Service("sessionService")
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

	private final CoreSessionService coreSessionService;

	private final BcHierarchyAware bcHierarchyAware;

	private final UserCache userCache;
	

	@Override
	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = {CacheConfig.REQUEST_CACHE}, key = "#root.methodName")
	public IUser<Long> getSessionUser() {
		return coreSessionService.getSessionUserDetails(true);
	}

	@Override
	public Long getSessionUserDepartmentId() {
		return getSessionUser().getDepartmentId();
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
		if (StringUtils.isBlank(requestedRole)) {
			return mainRole;
		}
		if (mainRole != null && requestedRole.equals(mainRole.getKey())) {
			return mainRole;
		}
	/*	LOV currentRole = userRoleService.getMatchedRole(getUserFromDetails(userDetails), requestedRole);
		if (currentRole == null) {
			currentRole = userDetails.getUserRole();
		}*/
		return  new LOV(requestedRole);
	}


	@Override
	public Map<String, Boolean> getResponsibilities() {
		return userCache.getResponsibilities(getSessionUser(), getSessionUserRole());
	}

	@Override
	public String getSessionId() {
		return coreSessionService.getSessionId();
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
		public Map<String, Boolean> getResponsibilities(final IUser<Long> user, final LOV userRole) {
			return uiService.getResponsibilities(
					user,
					userRole
			);
		}

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = {CacheConfig.USER_CACHE},
				key = "{#root.methodName, #screenName, #user.id, #userRole}"
		)
		public List<String> getViews(final String screenName, final IUser<Long> user, final LOV userRole) {
			return uiService.getViews(
					screenName,
					user,
					userRole
			);
		}

	}

}
