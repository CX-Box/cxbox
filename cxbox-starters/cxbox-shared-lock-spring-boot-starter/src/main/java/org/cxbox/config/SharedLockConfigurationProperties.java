package org.cxbox.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cxbox.shared-lock")
public class SharedLockConfigurationProperties {

	private long timeout = 1_800_000;

	private long checkInterval = 1000;
}
