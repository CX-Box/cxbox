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

	/**
	 * Timeout removal of the lock in milliseconds.
	 * It is possible to override the default value in application.yml     cxbox.shared-lock.timeout:
	 */
	private long timeout = 1_800_000;

	/**
	 * The interval for checking whether the lock status has changed is indicated in milliseconds.
	 * It is possible to override the default value in application.yml   cxbox.shared-lock.check-interval
	 */
	private long checkInterval = 1000;
}
