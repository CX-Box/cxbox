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
import org.cxbox.constgen.DtoField;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("cxbox.widget.fields")
public class WidgetFieldsIdResolverProperties {

	public static final String FILTER_BY_RANGE_ENABLED_DEFAULT_PARAM_NAME = "filterByRangeEnabled";

	public static final String SORT_ENABLED_DEFAULT_PARAM_NAME = "sortEnabled";

	/**
	 /**
	 * <code>sortEnabledDefault = false</code> - hides sort icon on all fields by default.
	 * <ul>
	 *     <li>To make individual field <code>sortable</code>, e.g. to show sort icon and enable user to change sort order with it - use {@link org.cxbox.core.dto.rowmeta.FieldsMeta#enableSort(DtoField[])}</li>
	 *     <li>To set <code>default sort order</code> on individual field - use {@link org.cxbox.meta.entity.BcProperties#sort} (usually is set with CSV <code>BC_PROPERTIES.CSV column SORT</code> in your project)</li>
	 *     <li>If field is not <code>sortable</code>, but has <code>default sort order</code> - sort icon will be shown and <code>default sort order</code> will be auto applied, but user will not be able to change sort order</li>
	 * </ul>
	 * <br>
	 * <code>sortEnabledDefault = true</code> (deprecated) - shows sort icon on all fields by default.
	 * <ul>
	 *     <li>Must not be used - added only for backward compatibility after migration on this version of CXBOX. You will not be able to make only individual field <code>sortable</code> or not <code>sortable</code> when <code>sortEnabledDefault = true</code> - migrate to <code>sortEnabledDefault = false</code> as soon as possible</li>
	 * </ul>
	 */
	private boolean sortEnabledDefault = false;

	/**
	 * Enabled filter by range for Date/DateTime/DateTimeWithSeconds fields
	 */
	private boolean filterByRangeEnabledDefault = false;


	private String[] includePackages = {"org.cxbox.meta.ui.model.json.field.subtypes"};

	private String[] excludeClasses = {};

}
