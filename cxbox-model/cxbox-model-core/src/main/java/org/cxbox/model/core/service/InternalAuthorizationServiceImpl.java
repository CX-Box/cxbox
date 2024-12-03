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

import org.cxbox.api.service.session.InternalAuthorizationService;
import org.cxbox.api.service.session.CxboxAuthenticationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public final class InternalAuthorizationServiceImpl implements InternalAuthorizationService {

	private final CxboxAuthenticationService cxboxAuthenticationService;

	public InternalAuthorizationServiceImpl(@Lazy CxboxAuthenticationService cxboxAuthenticationService) {
		this.cxboxAuthenticationService = cxboxAuthenticationService;
	}

	@Override
	public Authentication createAuthentication(final SystemUser systemUser) {
		return createAuthentication(systemUser.getLogin(), null);
	}

	@Override
	public Authentication createAuthentication(final String login, final String userRole) {
		final UserDetails userDetails = cxboxAuthenticationService.loadUserByUsername(login, userRole);
		return new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				userDetails.getAuthorities()
		);
	}

	@Override
	public void loginAs(final SystemUser systemUser) {
		loginAs(createAuthentication(systemUser));
	}

	@Override
	public void loginAs(final String login, final String userRole) {
		loginAs(createAuthentication(login, userRole));
	}

	@Override
	public void loginAs(final Authentication authentication) {
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}
