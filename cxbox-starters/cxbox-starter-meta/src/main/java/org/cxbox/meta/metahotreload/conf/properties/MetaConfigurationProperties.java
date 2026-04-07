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
	 * Views visible for user can depend on user roles.
	 * Allowed View-Role pairs are always taken from table responsibilities(internal_role, responsibilites, ...)
	 *  But one must always avoid loading this pairs by hand. Instead use one of 2 options:
	 *  1) viewAllowedRolesEnabled = true : auto load from *view.json->rollesAllowed tags
	 *  2) viewAllowedRolesEnabled = false (default) :auto load from standartized RESPONSIBILITIES.csv with liquibase
	 * Recommendations:
	 *  Always prefer viewAllowedRolesEnabled = true when possible due to better plugin support and faster development speed.
	 * Use viewAllowedRolesEnabled = false only when responsibilities table edit through admin UI is required in your project.
	 * During local development still to use viewAllowedRolesEnabled = true,
	 * then export the RESPONSIBILITIES.csv using /view/responsibilitiesAdmin (Export button)
	 * and set viewAllowedRolesEnabled = false before commit
	 */
	private boolean viewAllowedRolesEnabled = false;


	/**
	 * Actions visible for user can depend on user roles.
	 * Allowed Actions-Role  pairs are always taken from table responsibilities_action(internal_role, action, widget,view...)
	 *  But one must always avoid loading this pairs by hand. Instead use one of 2 options:
	 *  1) widgetActionGroupsEnabled (default) = true : auto load from *.widget.json->actionGroups tags
	 *  2) widgetActionGroupsEnabled= false :auto load from standartized RESPONSIBILITIES_ACTION.csv with liquibase
	 * Recommendations:
	 *  Always prefer widgetActionGroupsEnabled= true when possible due to better plugin support and faster development speed.
	 * Use widgetActionGroupsEnabled= false only when responsibilities_action table edit through admin UI is required in your project.
	 * During local development still to use widgetActionGroupsEnabled = true,
	 * then export the RESPONSIBILITIES_ACTION.csv using /view/responsibilitiesActionAdmin (Export button)
	 * and set widgetActionGroupsEnabled= false before commit
	 */
	private boolean widgetActionGroupsEnabled = true;

	/**
	 * !!! only for local development, no use on production
	 * Controls the loading mode for widget action buttons
	 * and determines the data population strategy for the `responsibility_action` table
	 * !!! Works when `widget-action-groups-enabled: true`
	 * 1) widgetActionGroupsCompact =`true` (default) – compact fill mode (fewer rows).
	 *                    Creates entries for each button of each widget,
	 *                    populating the role field with `*`
	 *                    and the view field with `*`
	 *                    (`*` means all roles/all views).
	 * 2) widgetActionGroupsCompact= `false` - full mode, separate row per role and view.
	 *
	 * <p><b>Example – ({@code true}):</b>
	 * <pre>
	 * id,internal_role_cd,action,view,widget
	 * 1100581,*,create,*,clientList
	 * </pre>
	 *
	 * <p><b>Example –({@code false}):</b>
	 * <pre>
	 * id,internal_role_cd,action,view,widget
	 * 1100692,ADMIN,create,clientlist,clientList
	 * 1100693,CXBOX_USER,create,clientlist,clientList
	 * 1100694,BUSINESS_ADMIN,create,clientlist,clientList
	 * </pre>
	 * Recommendations:
	 * In the early stages of a project, when the roles  for View and Action are not yet
	 * clearly defined, we recommend using this mode.
	 * This will significantly speed up the development of
	 * screen forms by enabling fast loading of all relationships.
	 */

	private boolean widgetActionGroupsCompact = true;

	/**
	 * Controls whether the {@code id} field is always included in API responses.
	 *
	 * <p>When set to {@code true} (default):
	 * The {@code id} field is always added to the response, even if it is not
	 * displayed in the UI for widgets of the corresponding Business Component (BC).
	 *
	 * <p>When set to {@code false}:
	 * If a widget is present on the view but has no fields rendered in the UI
	 * for the corresponding BC, the response will not include any data.
	 *
	 * <p>This property is used to control backward compatibility of API responses.
	 */

	@Deprecated
	private boolean alwaysIncludeIdEnabled = true;

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
