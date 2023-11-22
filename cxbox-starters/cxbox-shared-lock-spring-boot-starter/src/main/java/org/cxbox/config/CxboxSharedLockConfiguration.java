package org.cxbox.config;


import lombok.RequiredArgsConstructor;
import org.cxbox.StandardCxboxSharedLock;
import org.cxbox.core.metahotreload.CxboxSharedLock;
import org.cxbox.core.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.service.MetaLockService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({CxboxSharedLock.class})
@ConditionalOnMissingBean(value = CxboxSharedLock.class)
@RequiredArgsConstructor
public class CxboxSharedLockConfiguration {

	@Bean
	public CxboxSharedLock cxboxSharedLock(MetaConfigurationProperties config,
			MetaLockService metaLockService,
			JpaDao jpaDao) {
		return new StandardCxboxSharedLock(config, metaLockService, jpaDao);
	}

}
