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

package org.cxbox.core.config;

import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.core.file.conf.CxboxFileConfiguration;
import org.cxbox.core.metahotreload.conf.MetaHotReloadConfiguration;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.core.service.impl.ResponsibilitiesServiceImpl;
import org.cxbox.model.core.api.CurrentUserAware;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.core.service.BaseEntityListenerDelegate;
import org.cxbox.model.core.service.CxboxBaseEntityListenerDelegate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;


@EnableAspectJAutoProxy
@BeanScan({"org.cxbox"})
@EnableSpringConfigured
@Import({
		MetaHotReloadConfiguration.class,
		CxboxFileConfiguration.class
})
@EnableConfigurationProperties(CxboxBeanProperties.class)
public class CoreApplicationConfig {

	@Bean
	@ConditionalOnMissingBean
	public ResponsibilitiesService responsibilitiesService(JpaDao jpaDao) {
		return new ResponsibilitiesServiceImpl(jpaDao);
	}

	@Bean
	@ConditionalOnMissingBean
	public BaseEntityListenerDelegate baseEntityListenerDelegate(CurrentUserAware currentUserAware) {
		return new CxboxBaseEntityListenerDelegate(currentUserAware);
	}

}
