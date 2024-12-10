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

package org.cxbox.api.service.session;

import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;


public interface InternalAuthorizationService {

	SystemUser VANILLA = SystemUsers.VANILLA;

	Authentication createAuthentication(@NonNull SystemUser systemUser);

	Authentication createAuthentication(@NonNull final String login, @NonNull final Set<String> userRole);

	void loginAs(@NonNull SystemUser systemUser);

	void loginAs(@NonNull String login, @NonNull Set<String> userRole);

	void loginAs(@NonNull Authentication authentication);

	@RequiredArgsConstructor
	@Getter
	enum SystemUsers implements SystemUser {

		VANILLA("vanilla", 1L);

		private final String login;

		private final Long id;

	}

	interface SystemUser {

		String getLogin();

		Long getId();

	}


}
