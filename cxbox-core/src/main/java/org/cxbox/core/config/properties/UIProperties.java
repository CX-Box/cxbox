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

package org.cxbox.core.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("cxbox.ui")
public class UIProperties {

	public static final String SIDE_BAR_WORD_BREAK = "sideBarWordBreak";

	public static final String SIDE_BAR_SEARCH_ENABLE = "sideBarSearchEnabled";

	public static final String NOTIFICATION_MODE = "notificationMode";

	public static final String APP_INFO_DESCRIPTION = "infoDescription";

	public static final String APP_INFO_ENV = "infoEnv";

	public static final String APP_INFO_COLOR = "infoColor";

	/**
	 * * useServletContextPath = true is deprecated, and it means you will create 2 servlets (for api with context-path = '/api/v1' and for ui with context-path = '/ui'). This is very complex and non-common approach for springboot apps.
	 * useServletContextPath = false, means your app have only 1 servlet with context-path = '' , so cxbox needs to add '/api/v1' prefix to rest controllers explicitly. Also cxbox will configure ui static content delivery in a slightly different way
	 */
	@Deprecated
	private Boolean useServletContextPath = false;
	public static final String DRILL_DOWN_TOOLTIP_NAME = "drillDownTooltip";

	public static final String MULTI_ROLE_ENABLED  = "multiRoleEnabled";

	/**
	 * * Use only when useServletContextPath = false;
	 */
	private String path = "/ui";

	/**
	 * * Lock row in db timeout in ms; -1 by default, e.g. infinite
	 */
	private int uiLockTimeoutMs = -1;

	private String systemUrl = null;

	/**
	 * Enabled the transfer of long names of side menu values
	 * <br>
   * Available values:<br>
   * - none: Default setting. No word breaking occurs; text will not wrap.<br>
   * - auto: Automatic word breaking. Text in the sidebar will wrap to the next line as needed.
	 */
	private String sideBarWordBreak = "none";

	private Boolean sideBarSearchEnabled = true;

	/**
	 * The mode of notification display in the application.
	 *<br>
	 * Available values:<br>
	 * - single: Displays a single notification panel that updates with new notifications.<br>
	 * - column: Displays each notification in a separate panel stacked vertically.<br>
	 * - stack: Displays notifications in overlapping panels.<br>
	 */

	private String notificationMode = "single";

	private String drillDownTooltip = null;

	private boolean multiRoleEnabled = false;

	/**
	 * Description of the application, including the version or any other relevant information.
	 * <br>
	 * A string containing information about the application, e.g., "Version 1.0.0"<br>
	 * If the value is null, it will not be displayed on the frontend
	 */

	private String appInfoDescription = null;

	/**
	 * The type of environment in which the application is running.<br>
	 * For example, it could be "staging", "production", or "testing".<br>
	 * If the value is null, it will not be displayed on the frontend
	 */
	private String appInfoEnv = null;

	/**
	 * The background color used in the application. This parameter is optional.
	 * <br>
	 * A string representing the background color in HEX format or a named color
	 *         (e.g., "#FFFFFF" for white or "red" for red).<br>
	 * If the value is null, the color will be set to #262626, matching the sidebar.
	 */
	private String appInfoColor = null;

}
