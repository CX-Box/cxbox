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

package org.cxbox.meta;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.ScreenResponsibilityService;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.service.session.IUser;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.dto.ScreenResponsibility;
import org.cxbox.meta.metahotreload.repository.UserMetaProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScreenResponsibilityServiceImpl implements ScreenResponsibilityService {

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper objectMapper;

	private final ResponsibilitiesService respService;

	private final UserMetaProvider userMetaProvider;

	/**
	 * Get all available screens with respect of user role
	 *
	 * @param user Active session user
	 * @param userRole User role
	 * @return JsonNode Available screens
	 */
	@Override
	public List<ScreenResponsibility> getScreens(IUser<Long> user, String userRole) {
		var allOverrides = respService.getOverrideScreensResponsibilities(user, userRole);
		Map<String, ScreenResponsibility> allUserScreens = userMetaProvider.getAvailableScreensResponsibilities(
				user,
				userRole
		);
		allOverrides.forEach(override -> allUserScreens.computeIfPresent(
						override.getName(),
						(key, old) -> new ScreenResponsibility()
								.setId(old.getId())
								.setUrl(old.getUrl())
								.setMeta(old.getMeta())
								.setName(override.getName())
								.setOrder(Optional.ofNullable(override.getOrder()).orElse(Optional.ofNullable(old.getOrder()).orElse(0)))
								.setText(Optional.ofNullable(override.getText()).orElse(old.getText()))
								.setIcon(Optional.ofNullable(override.getIcon()).orElse(old.getIcon()))

				)
		);
		return allUserScreens.values().stream()
				.sorted(Comparator.comparing(ScreenResponsibility::getOrder).thenComparing(ScreenResponsibility::getName))
				.toList();
	}

}
