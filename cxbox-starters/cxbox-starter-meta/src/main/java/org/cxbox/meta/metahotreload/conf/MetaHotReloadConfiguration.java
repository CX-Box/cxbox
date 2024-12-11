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

package org.cxbox.meta.metahotreload.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.service.session.InternalAuthorizationService;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.MetaHotReloadService;
import org.cxbox.dictionary.DictionaryProvider;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.metahotreload.service.MetaHotReloadServiceImpl;
import org.cxbox.meta.metahotreload.service.MetaResourceReaderService;
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
			@Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objMapper) {
		return new MetaResourceReaderService(applicationContext, config, objMapper);
	}

	@Bean
	public MetaHotReloadService refreshMeta(
			MetaConfigurationProperties config,
			MetaResourceReaderService metaResourceReaderService,
			InternalAuthorizationService authzService,
			TransactionService txService,
			MetaRepository metaRepository,
			Optional<DictionaryProvider> dictionaryProvider) {
		return new MetaHotReloadServiceImpl(
				config,
				metaResourceReaderService,
				authzService,
				txService,
				metaRepository,
				dictionaryProvider);
	}
}
