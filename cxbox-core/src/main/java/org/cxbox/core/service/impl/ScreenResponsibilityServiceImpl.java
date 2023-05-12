
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

package org.cxbox.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.core.dto.data.view.ScreenDTO;
import org.cxbox.core.dto.data.view.ScreenResponsibility;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.core.service.ScreenResponsibilityService;
import org.cxbox.core.service.UIService;
import org.cxbox.core.service.ViewService;
import org.cxbox.model.core.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScreenResponsibilityServiceImpl implements ScreenResponsibilityService {

	@Qualifier("cxboxObjectMapper")
	private final ObjectMapper objectMapper;

	private final ResponsibilitiesService respService;

	private final UIService uiService;

	private final ViewService viewService;

	/**
	 * Get all available screens with respect of user role
	 *
	 * @param user Active session user
	 * @param userRole User role
	 * @return JsonNode Available screens
	 */
	@Override
	public List<ScreenResponsibility> getScreens(User user, LOV userRole) {
		List<ScreenResponsibility> result = new ArrayList<>();
		try {
			String screens = respService.getListScreensByUser(user, userRole);
			if (StringUtils.isNotBlank(screens)) {
				result.addAll(objectMapper.readValue(screens, ScreenResponsibility.LIST_TYPE_REFERENCE));
			}
			List<ScreenResponsibility> commonScreens = uiService.getCommonScreens();
			if (commonScreens != null) {
				result.addAll(commonScreens);
			}
			result.forEach(resp -> {
				String screenName = resp.getName();
				ScreenDTO screenDto = viewService.getScreen(screenName);
				resp.setMeta(screenDto);
			});
			return result;
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}

		return result;
	}
}
