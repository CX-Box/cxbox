package org.cxbox.config.autoconfig;


import lombok.RequiredArgsConstructor;
import org.cxbox.config.SharedLockConfigurationProperties;
import org.cxbox.core.metahotreload.CxboxSharedLock;
import org.cxbox.service.MetaLockService;
import org.cxbox.service.StandardCxboxSharedLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnClass({CxboxSharedLock.class})
@ConditionalOnMissingBean(value = CxboxSharedLock.class)
@EnableConfigurationProperties({SharedLockConfigurationProperties.class})
@EnableJpaRepositories(basePackages = "org.cxbox.repository")
@RequiredArgsConstructor
public class CxboxSharedLockConfiguration {

	@Bean
	public CxboxSharedLock cxboxSharedLock(SharedLockConfigurationProperties config,
			MetaLockService metaLockService) {
		return new StandardCxboxSharedLock(config, metaLockService);
	}

}
