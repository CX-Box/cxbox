/*-
 * #%L
 * IO Cxbox - Core
 * %%
 * Copyright (C) 2018 - 2020 Cxbox Contributors
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

package org.cxbox.core.controller;

import static org.cxbox.core.config.properties.APIProperties.CXBOX_API_PATH_SPEL;

import org.cxbox.core.dto.data.view.ScreenResponsibility;
import org.cxbox.core.service.ScreenResponsibilityService;
import org.cxbox.core.util.session.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(CXBOX_API_PATH_SPEL)
public class ScreenController {

	private final ScreenResponsibilityService screenResponsibilityService;

	private final SessionService sessionService;

	/**
	 * Should be called by authenticated user for a list of available screens
	 *
	 * @return Available screens and their meta information
	 */
	@GetMapping("/screens")
	public List<ScreenResponsibility> getScreens() {
		return screenResponsibilityService.getScreens(sessionService.getSessionUser(), sessionService.getSessionUserRole());
	}
}
