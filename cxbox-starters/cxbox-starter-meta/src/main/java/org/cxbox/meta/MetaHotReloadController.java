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

import java.util.Optional;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.config.cache.CxboxCachingService;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.MetaHotReloadService;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.meta.metafieldsecurity.BcUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(CXBOX_API_PATH_SPEL + "/bc-registry")
public class MetaHotReloadController {

	final Optional<MetaHotReloadService> metaHotReloadService;

	final TransactionService txService;

	final CxboxCachingService cachingService;

	final  BcRegistry bcRegistry;

	final  BcUtils bcUtils;

	@GetMapping("refresh-meta")
	public void refresh() {
		if (metaHotReloadService.isPresent()) {
		metaHotReloadService.get().loadMeta();
		txService.invokeAfterCompletion(Invoker.of(
				() -> {
					cachingService.evictUiCache();
					cachingService.evictRequestCache();
					cachingService.evictUserCache();
				}
		));
		}
	}

}
