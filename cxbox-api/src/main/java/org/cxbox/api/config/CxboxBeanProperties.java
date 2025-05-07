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

package org.cxbox.api.config;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cxbox.bean")
public class CxboxBeanProperties {

	/**
	 * Default date used for filling in time sorting.
	 * <p>
	 * This variable holds the maximum possible date {@link LocalDate}, which can be used
	 * in situations where the actual date is not specified or when a baseline date is needed
	 * for sorting. The value {@link LocalDate#EPOCH} = '1970-01-01'  represents the latest possible day in the calendar,
	 * helping to avoid errors when comparing dates.
	 * </p>
	 *
	 * <p>
	 * This variable is passed to the front end to ensure the correct functioning
	 * of the user interface and time sorting within the application.
	 * It is important to document the use of this date clearly
	 * to prevent confusion when handling data on the client side.
	 * </p>
	 */
	public static final String DEFAULT_DATE  = "defaultDate";

	/**
	 * We use "cxboxObjectMapper" to allow projects to define separate object mappers on project level and to avoid conflicts in this cases
	 */
	public static final String OBJECT_MAPPER = "cxboxObjectMapper";

	/**
	 * We use "dataSource" by default to ease springboot-starter-jpa (and so on) integration out of the box.
	 * Set "primaryDS" legacy value explicitly, if you need this.
	 */
	private String dataSource = "dataSource";

	/**
	 * We use "entityManagerFactory" by default to ease springboot-starter-jpa (and so on) integration out of the box.
	 * Set "cxboxEntityManagerFactory" legacy value explicitly, if you need this.
	 */
	private String entityManagerFactory = "entityManagerFactory";

	@DateTimeFormat(pattern = "yyyy.MM.dd")
	private LocalDate defaultDate = LocalDate.EPOCH;
}
