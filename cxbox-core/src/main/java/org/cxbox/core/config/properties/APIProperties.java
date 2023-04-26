/*-
 * #%L
 * IO Cxbox - Core
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.core.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("cxbox.api")
public class APIProperties {

	public static final String CXBOX_API_PATH_SPEL = "#{ (${cxbox.api.use-servlet-context-path} == true) ? '':  '${cxbox.api.path}'}";

	/**
	 ** useServletContextPath = true is deprecated, and it means you will create 2 servlets (for api with context-path = '/api/v1' and for ui with context-path = '/ui'). This is very complex and non-common approach for springboot apps.
	 * useServletContextPath = false, means your app have only 1 servlet with context-path = '' , so cxbox needs to add '/api/v1' prefix to rest controllers explicitly. Also cxbox will configure ui static content delivery in a slightly different way
	 */
	@Deprecated
	private Boolean useServletContextPath = false;

	/**
	** Use only when useServletContextPath = false;
	 */
	private String path = "/api/v1";

}
