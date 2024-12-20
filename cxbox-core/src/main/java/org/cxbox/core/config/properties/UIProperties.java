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

	public static final String DRILL_DOWN_TOOLTIP_NAME = "drillDownTooltip";

	public static final String MULTI_ROLE_ENABLED  = "multiRoleEnabled";

	/**
	 ** Use only when useServletContextPath = false;
	 */
	private String path = "/ui";

	/**
	 ** Lock row in db timeout in ms; -1 by default, e.g. infinite
	 */
	private int uiLockTimeoutMs = -1;

	private String systemUrl = null;

	private String drillDownTooltip = null;

	private boolean multiRoleEnabled = false;

}
