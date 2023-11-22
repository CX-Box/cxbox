package org.cxbox.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cxbox.meta")
public class SharedLockConfigurationProperties {

	private int baseLockTimer = 1800;

	private long checkLockInterval = 1000;
}
