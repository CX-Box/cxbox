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
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.core.config.BeanScan;
import org.cxbox.core.config.properties.WidgetFieldsIdResolverProperties;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.ui.field.PackageScanFieldIdResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;


@EnableAspectJAutoProxy
@BeanScan({"org.cxbox"})
@EnableSpringConfigured
@EnableConfigurationProperties(CxboxBeanProperties.class)
public class MetaApplicationConfig {

	@Bean
	@ConditionalOnMissingBean
	public ResponsibilitiesService responsibilitiesService(MetaRepository metaRepository, CacheManager cacheManager, TransactionService txService, @Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objectMapper) {
		return new ResponsibilitiesServiceImpl(metaRepository, cacheManager, txService, objectMapper);
	}


	@Bean
	public PackageScanFieldIdResolver packageScanFieldIdResolver(WidgetFieldsIdResolverProperties properties) {
		return new PackageScanFieldIdResolver(properties);
	}


}
