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

import static org.cxbox.api.service.session.InternalAuthorizationService.VANILLA;

import org.cxbox.api.service.session.CoreSessionService;
import org.cxbox.api.service.session.CxboxUserDetailsInterface;
import org.cxbox.core.util.session.SessionUser;
import org.cxbox.core.util.session.UserExternalService;
import org.cxbox.core.util.session.UserService;
import org.cxbox.model.core.api.EffectiveUserAware;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.core.entity.User;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class EffectiveUserAwareImpl implements EffectiveUserAware<User>  {

	private final Optional<List<UserExternalService>> userExternalServices;

	private final UserService userService;

	private final JpaDao jpaDao;

	private final CoreSessionService coreSessionService;

	/**
	 * get current User entity from CoreSessionService, if not found try to found User in
	 * UserExternalService's (that defined by client applications).
	 * @param fallbackToSystem - if enabled, empty authenticated user replaced with system VANILLA user
	 * @return User entity
	 */
	private User getSessionUserInternal(boolean fallbackToSystem) {
		CxboxUserDetailsInterface details = coreSessionService.getSessionUserDetails(false);
		if (details != null) {
			return getUserFromDetails(details);
		}
		SessionUser sessionUser = null;
		if (userExternalServices.isPresent()) {
			for (UserExternalService userExternalService : userExternalServices.get()) {
				sessionUser = userExternalService.getSessionUser();
				if (sessionUser != null) {
					break;
				}
			}
		}
		if (sessionUser == null) {
			throw new SessionAuthenticationException("Not authorized");
		}
		User user = userService.getUserByLogin(sessionUser.getId());
		if (user == null && fallbackToSystem) {
			// here the user has already authenticated
			// therefore it seems normal to replace it with a system user
			user = new User();
			user.setId(VANILLA.getId());
		}
		return user;
	}

	@Override
	public User getEffectiveSessionUser() {
		return getSessionUserInternal(true);
	}

	private User getUserFromDetails(final CxboxUserDetailsInterface userDetails) {
		return jpaDao.findById(User.class, userDetails.getId());
	}

}
