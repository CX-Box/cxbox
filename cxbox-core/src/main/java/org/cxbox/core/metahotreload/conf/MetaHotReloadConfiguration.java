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

package org.cxbox.core.metahotreload.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.cxbox.api.service.session.InternalAuthorizationService;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.core.metahotreload.CxboxSharedLock;
import org.cxbox.core.metahotreload.MetaHotReloadService;
import org.cxbox.core.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.core.metahotreload.service.*;
import org.cxbox.model.core.dao.JpaDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(MetaConfigurationProperties.class)
@Configuration
public class MetaHotReloadConfiguration {

	@Bean
	MetaResourceReaderService metaResourceReaderService(
			ApplicationContext applicationContext,
			MetaConfigurationProperties config,
			@Qualifier("cxboxObjectMapper") ObjectMapper objMapper) {
		return new MetaResourceReaderService(applicationContext, config, objMapper);
	}

	@Bean
	public MetaHotReloadService refreshMeta(
			MetaConfigurationProperties config,
			MetaResourceReaderService metaResourceReaderService,
			InternalAuthorizationService authzService,
			TransactionService txService,
			JpaDao jpaDao,
			WidgetUtil widgetUtil,
			ViewAndViewWidgetUtil viewAndViewWidgetUtil,
			ScreenAndNavigationGroupAndNavigationViewUtil screenAndNavigationGroupAndNavigationViewUtil,
			BcUtil bcUtil,
			Optional<CxboxSharedLock> cxboxSharedLock) {
		return new MetaHotReloadServiceImpl(
				config,
				metaResourceReaderService,
				authzService,
				txService,
				jpaDao,
				widgetUtil,
				viewAndViewWidgetUtil,
				screenAndNavigationGroupAndNavigationViewUtil,
				bcUtil,
				cxboxSharedLock
		);
	}
}
