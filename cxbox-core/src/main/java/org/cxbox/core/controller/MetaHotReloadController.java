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

import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.config.cache.CxboxCachingService;
import org.cxbox.core.metahotreload.MetaHotReloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(CXBOX_API_PATH_SPEL + "/bc-registry")
public class MetaHotReloadController {

	final MetaHotReloadService metaHotReloadService;

	final TransactionService txService;

	final CxboxCachingService cachingService;

	/*TODO>>test and uncomment
	final BcUtils bcUtils;

	final BcRegistry bcRegistry;
	*/

	@GetMapping("refresh-meta")
	public void refresh() {
		metaHotReloadService.loadMeta();
		txService.invokeAfterCompletion(Invoker.of(
				() -> {
					cachingService.evictUiCache();
					cachingService.evictRequestCache();
					cachingService.evictUserCache();
					/*TODO>>test and uncomment
					bcRegistry.refresh();
					bcUtils.invalidateFieldCache();*/
				}
		));
	}

}
