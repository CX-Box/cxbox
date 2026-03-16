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

package org.cxbox.meta.metahotreload.conf.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cxbox.meta")
public class MetaConfigurationProperties {

	private boolean devPanelEnabled = false;
	/**
	 * Determines where the allowed roles for views are loaded from.
	 * true  – allowed roles for views are taken from JSON configuration files.
	 * false – allowed roles are loaded from CSV files.
	 */
	private boolean viewAllowedRolesEnabled = false;


	/**
	 * Determines where widget action buttons are loaded from.
	 * true  – widget buttons (action groups) are taken only from JSON configuration files.
	 * false – widget buttons are taken from both:
	 * CSV files
	 * JSON files
	 * Limitation: when this option is disabled (false), buttons defined in JSON
	 * will not be included in the final configuration file exported from the UI.
	 * This flag is typically used during migration from JSON-based configuration
	 * to CSV-based configuration.
	 */
	private boolean widgetActionGroupsEnabled = true;

	private boolean widgetActionGroupsCompact = true;

	@NotNull(message = "Path to meta files directory. Supports file: or classpath: prefix. "
			+ "Example of usage is: applicationContext.getResources(directory + widgetPath)")
	private String directory = "classpath*:db/migration/liquibase/data/latest";

	@NotBlank(message = "Path to widget files from MetaConfigurationProperties.directory")
	private String widgetPath = "/**/*.widget.json";

	@NotBlank(message = "Path to view files from MetaConfigurationProperties.directory")
	private String viewPath = "/**/*.view.json";

	@NotBlank(message = "Path to screen files from MetaConfigurationProperties.directory")
	private String screenPath = "/**/*.screen.json";

	@NotBlank(message = "Path to sqlbc files from MetaConfigurationProperties.directory")
	private String bcPath = "/**/*.sqlbc.json";

	@Positive(message  =  "Number of rows on bc by default (can be override with BC_PROPERTIES.csv)")
	private Long bcDefaultPageLimit = 5L;

}
