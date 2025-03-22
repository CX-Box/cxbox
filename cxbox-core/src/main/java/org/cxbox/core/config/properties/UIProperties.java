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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("cxbox.ui")
public class UIProperties {

	public static final String SIDE_BAR_WORD_BREAK = "sideBarWordBreak";

	public static final String SIDE_BAR_SEARCH_ENABLE = "sideBarSearchEnabled";

	public static final String NOTIFICATION_MODE = "notificationMode";

	public static final String DRILL_DOWN_TOOLTIP_NAME = "drillDownTooltip";

	public static final String MULTI_ROLE_ENABLED  = "multiRoleEnabled";

	public static final String APP_INFO_ENV = "appInfoEnv";

	public static final String APP_INFO_DESCRIPTION = "appInfoDescription";

	public static final String APP_INFO_COLOR = "appInfoColor";

	/**
	 * useServletContextPath = true is deprecated, and it means you will create 2 servlets
	 * (for api with context-path = '/api/v1' and for ui with context-path = '/ui').
	 * This is a very complex and non-common approach for springboot apps.
	 * useServletContextPath = false means your app has only 1 servlet with a context-path = '',
	 * so cxbox needs to add '/api/v1' prefix to rest controllers explicitly.
	 * Also, cxbox will configure ui static content delivery in a slightly different way
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	private Boolean useServletContextPath = false;

	/**
	 * Use only when useServletContextPath = false;
	 */
	private String path = "/ui";

	/**
	 * Lock row in db timeout in ms; -1 by default, e.g., infinite
	 */
	@Min(value = -1)
	private int uiLockTimeoutMs = -1;

	private String systemUrl = null;

	/**
	 * Enabled to transfer long screen names in a sidebar menu
	 * <br>Supported standard values:</br>
   * - "none": default setting. No word breaking occurs; text will not wrap.<br>
   * - "auto": automatic word breaking. Text in the sidebar will wrap to the next line as needed.
	 */
	@NotBlank
	private String sideBarWordBreak = "none";

	private Boolean sideBarSearchEnabled = true;

	/**
	 * Web socket notification display mode.
	 * <br>Supported standard values:</br>
	 * - "single": displays a single notification panel that updates with new notifications.<br>
	 * - "column": displays each notification in a separate panel stacked vertically.<br>
	 * - "stack": displays notifications in overlapping panels.<br>
	 */
	@NotBlank
	private String notificationMode = "single";

	/**
	 * Tooltip displayed on mouse hover over any drillDown link.
	 * <br>Supported standard values:</br>
	 * - "newAndCopy": tooltip with 2 icons -
	 * to open drillDown link in the new browser tab and to copy drillDown link to buffer.<br>
	 * If the value is null, no tooltip will be shown
	 */
	private String drillDownTooltip = null;

	private boolean multiRoleEnabled = false;

	/**
	 * Application info panel - current environment.<br>
	 * For example, it could be "DEV", "TEST", "PROD" and e.t.c.
	 * We recommend to short (<5 symbols) names, that can be shown on collapsed sidebar<br>
	 * If the value is null, no environment text will be shown
	 */
	private String appInfoEnv = null;

	/**
	 * Application info panel - description. Including the version or any other relevant information.
	 * <br>A string containing information about the application, e.g., "v1.0.0"</br>
	 * If the value is null, no description text will be shown
	 */
	private String appInfoDescription = null;

	/**
	 * Application info panel - background color.
	 * <br> A string representing the background color in HEX format (e.g., "#FFFFFF").</br>
	 * If the value is null, bo background color will be applied
	 */
	private String appInfoColor = null;

}
