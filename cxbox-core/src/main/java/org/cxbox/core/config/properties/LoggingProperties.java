/*
 * © OOO "SI IKS LAB", 2022-2025
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

package org.cxbox.core.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.logging.LogLevel;

/**
 * Configuration properties for application logging behavior.
 *
 * <p>This class manages logging levels and handlers across the application,
 * enabling fine-grained control over how business exceptions are logged
 * through the {@link org.cxbox.core.controller.GlobalExceptionHandler}.
 *
 * <p><b>Important:</b> When creating a custom exception that inherits from
 * {@link org.cxbox.core.exception.BusinessException}, implement
 * {@link org.cxbox.core.exception.LoggableBusinessException} to ensure
 * it is logged according to standard rules.
 *
 * <p>Configuration example:
 * <pre>
 * cxbox.logging.globalHandler.businessException.logLevel=INFO
 * </pre>
 *
 * @see LogLevel
 * @see GlobalHandler
 * @see BusinessException
 * @see org.cxbox.core.controller.GlobalExceptionHandler
 * @see org.cxbox.core.exception.BusinessException
 * @see org.cxbox.core.exception.LoggableBusinessException
 */
@Getter
@Setter
@ConfigurationProperties("cxbox.logging")
public class LoggingProperties {

	/**
	 * Global exception handler settings.
	 */
	private GlobalHandler globalHandler = new GlobalHandler();

	/**
	 * Configuration for {@link org.cxbox.core.controller.GlobalExceptionHandler}.
	 *
	 * <p>Defines how different types of exceptions are handled and logged
	 * at the application level.
	 */
	@Getter
	@Setter
	public static class GlobalHandler {
		private BusinessException businessException = new BusinessException();
	}

	/**
	 * Settings for logging {@link org.cxbox.core.exception.BusinessException}.
	 *
	 * <p>Controls the logging behavior of business exceptions caught by the global handler {@link org.cxbox.core.exception.LoggableBusinessException}.
	 * The logging level is determined based on the exception type and its configuration.
	 */
	@Getter
	@Setter
	public static class BusinessException {

		/**
		 * Log level for {@link org.cxbox.core.exception.BusinessException} instances
		 * on {@link org.cxbox.core.controller.GlobalExceptionHandler}.
		 *
		 * <p>Logging behavior:
		 * <ul>
		 *   <li>If the exception implements {@link org.cxbox.core.exception.LoggableBusinessException},
		 *       it will be logged at the standard level ({@code LogLevel.WARN})</li>
		 *   <li>If the exception does not implement {@link org.cxbox.core.exception.LoggableBusinessException},
		 *       it will be logged at the level specified by this property</li>
		 * </ul>
		 *
		 * <p>Default value: {@code LogLevel.WARN}
		 * <p><b>WARN!</b> if used SIEM, please use same level logging </p>
		 * @see LogLevel
		 * @see org.cxbox.core.exception.LoggableBusinessException
		 */
		private LogLevel logLevel = LogLevel.WARN;
	}

}
