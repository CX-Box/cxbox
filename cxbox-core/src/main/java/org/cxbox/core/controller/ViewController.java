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

package org.cxbox.core.controller;

import static org.cxbox.core.config.properties.APIProperties.CXBOX_API_PATH_SPEL;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.dto.ResponseDTO;
import org.cxbox.core.service.UIService;
import org.cxbox.core.service.ViewService;
import org.cxbox.core.ui.BcUtils;
import org.cxbox.core.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(CXBOX_API_PATH_SPEL)
public class ViewController {

	@Qualifier("cxboxObjectMapper")
	private final ObjectMapper mapper;

	private final ViewService views;

	private final BcUtils bcUtils;

	private final BcRegistry bcRegistry;

	private final UIService uiService;

	@RequestMapping(method = RequestMethod.GET, value = {"/screen/{name}/**", "/screen/{name}"})
	public ResponseDTO screen(@PathVariable String name) {
		return ResponseBuilder.build(views.getScreen(name));
	}

	private void invalidateCache() {
		bcRegistry.refresh();
		bcUtils.invalidateFieldCache();
		uiService.invalidateCache();
	}

}
