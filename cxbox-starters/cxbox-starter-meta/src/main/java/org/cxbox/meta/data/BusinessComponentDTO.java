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

package org.cxbox.meta.data;

import java.io.Serializable;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = {"name", "url"})
public class BusinessComponentDTO implements Serializable {

	String url;

	@JsonRawValue
	String defaultFilter;

	@JsonRawValue
	String dimFilterSpec;

	List<FilterGroupDTO> filterGroups = new ArrayList<>();

	String defaultSort;

	String cursor;

	Long page;

	/**
	 * default value is configured in {@link MetaConfigurationProperties#bcDefaultPageLimit bcDefaultPageLimit}
	 */
	Long limit;

	/**
	 * default value is limit
	 */
	Long massLimit;

	Boolean hasNext;

	String parentName;

	String name;

	Long reportPeriod;

	@JsonRawValue
	String binds;

	Boolean refresh;

	public BusinessComponentDTO(BcSource widgetDTO) {
		this.name = widgetDTO.getBcName();
		String url = widgetDTO.getUrl();
		this.url = url != null && url.charAt(url.length() - 1) == '/' ? url.substring(0, url.length() - 1) : url;
		this.cursor = "";
		this.page = 1L;
		this.hasNext = false;
	}

}
