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

package org.cxbox.meta.metahotreload.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;

@Setter
@Getter
public class WidgetSourceDTO {

	public static final String OPTIONS_PROP = "options";

	public static final String ACTION_GROUPS_PROP = "actionGroups";

	public static final String INCLUDE_PROP = "include";

	//specify only "name" instead
	@Deprecated
	String id;

	private String name;

	private String title;

	private String type;

	private String bc;

	private String template;

	private JsonNode showCondition;

	private JsonNode fields;

	private JsonNode axisFields;

	private JsonNode chart;

	/**
	 * Extendable widget part
	 * <br>
	 * One of parts is: <code>{@link  org.cxbox.meta.metahotreload.dto.WidgetSourceDTO#ACTION_GROUPS_PROP}</code> that contains visible actions in <code>{@link org.cxbox.meta.metahotreload.dto.WidgetSourceDTO#INCLUDE_PROP}</code>. Used only when <code>{@link MetaConfigurationProperties#widgetActionGroupsEnabled}</code> is true
	 */
	@JsonProperty(OPTIONS_PROP)
	private JsonNode options;

	private JsonNode graph;

	private JsonNode pivotFields;

	public String getWidgetNaturalKey() {
		return Optional.ofNullable(this.id).orElse(this.name);
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Options {

		@JsonProperty(ACTION_GROUPS_PROP)
		private ActionGroupsDTO actionGroups;

	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ActionGroupsDTO {

		@JsonProperty(INCLUDE_PROP)
		private List<Object> include;

	}
}
