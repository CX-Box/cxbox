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

package org.cxbox.core.util.session;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.CoreSessionService;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.cxbox.api.service.session.CxboxUserDetailsInterface;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;

@Service(CoreSessionService.SERVICE_NAME)
public class CoreSessionServiceImpl implements CoreSessionService {

	@Override
	public String getSessionId() {
		return RequestContextHolder.currentRequestAttributes().getSessionId();
	}

	@Override
	public CxboxUserDetailsInterface getSessionUserDetails(boolean raiseError) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			if (raiseError) {
				throw new SessionAuthenticationException("Not authorized");
			}
			return null;
		}

		CxboxUserDetailsInterface userDetails = getAuthenticationDetails(auth);

		if (userDetails == null) {
			if (raiseError) {
				throw new SessionAuthenticationException("Not authorized");
			}
		}

		return userDetails;
	}

	@Override
	public CxboxUserDetailsInterface getAuthenticationDetails(Authentication auth) {
		if (auth == null) {
			return null;
		}
		return Stream.of(auth.getDetails(), auth.getPrincipal())
				.filter(CxboxUserDetailsInterface.class::isInstance)
				.map(CxboxUserDetailsInterface.class::cast)
				.findFirst().orElse(null);
	}

	@Override
	public TimeZone getTimeZone(TimeZone defaultValue) {
		return Optional.ofNullable(getSessionUserDetails(false))
				.map(CxboxUserDetailsInterface::getTimezone)
				.map(LOV::getKey)
				.map(StringUtils::parseTimeZoneString)
				.orElse(defaultValue);
	}

	@Override
	public Locale getLocale(Locale defaultValue) {
		return Optional.ofNullable(getSessionUserDetails(false))
				.map(CxboxUserDetailsInterface::getLocaleCd)
				.map(LOV::getKey)
				.map(StringUtils::parseLocaleString)
				.orElse(defaultValue);
	}

}
