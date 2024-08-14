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

import static org.cxbox.core.config.properties.APIProperties.CXBOX_API_PATH_SPEL;

import lombok.RequiredArgsConstructor;
import org.cxbox.core.config.cache.CxboxCachingService;
import org.cxbox.core.dto.ResponseDTO;
import org.cxbox.core.util.ResponseBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(CXBOX_API_PATH_SPEL + "/bc-registry")
public class BcRegistryController {

	private final CxboxCachingService cachingService;

	@GetMapping(value = "invalidate-cache")
	public ResponseDTO invalidateCache() {
		cachingService.evictUiCache();
		cachingService.evictRequestCache();
		cachingService.evictUserCache();
		return ResponseBuilder.build();
	}

}
