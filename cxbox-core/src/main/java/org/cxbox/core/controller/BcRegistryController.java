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

package org.cxbox.core.controller;

import static org.cxbox.core.config.properties.APIProperties.CXBOX_API_PATH_SPEL;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.dto.ResponseDTO;
import org.cxbox.core.service.UIService;
import org.cxbox.core.ui.BcUtils;
import org.cxbox.core.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CXBOX_API_PATH_SPEL + "/bc-registry")
public class BcRegistryController {

	@Autowired
	private BcRegistry bcRegistry;

	@Autowired
	private BcUtils bcUtils;

	@Autowired
	private UIService uiService;

	@RequestMapping(method = GET, value = "invalidate-cache")
	public ResponseDTO invalidateCache() {
		bcRegistry.refresh();
		bcUtils.invalidateFieldCache();
		uiService.invalidateCache();
		return ResponseBuilder.build();
	}

}
