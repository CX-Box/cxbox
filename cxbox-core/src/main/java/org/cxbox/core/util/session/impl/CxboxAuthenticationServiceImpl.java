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

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.CxboxAuthenticationService;
import org.cxbox.api.service.session.CxboxUserDetails;
import org.cxbox.api.service.session.CxboxUserDetailsInterface;
import org.cxbox.core.service.impl.UserRoleService;
import org.cxbox.core.util.session.UserService;
import org.cxbox.model.core.entity.User;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CxboxAuthenticationServiceImpl implements CxboxAuthenticationService {

	private final UserService userService;

	private final UserRoleService userRoleService;

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		return loadUserByUsername(username, null);
	}

	@SuppressWarnings("java:S5804")
	@Override
	public UserDetails loadUserByUsername(final String username, final LOV userRole) throws UsernameNotFoundException {
		final User user = userService.getUserByLogin(username);
		if (user == null) {
			throw new UsernameNotFoundException(username);
		}
		return createUserDetails(
				user,
				userRole == null
						? userRoleService.getMainUserRoleKey(user)
						: userRole
		);
	}

	private CxboxUserDetailsInterface createUserDetails(final User user, final LOV userRole) {
		return CxboxUserDetails.builder()
				.id(user.getId())
				.username(user.getLogin())
				.password(user.getPassword())
				.userRole(userRole)
				.timezone(user.getTimezone())
				.localeCd(user.getLocale())
				.authorities(Collections.emptySet())
				.build();
	}

}
