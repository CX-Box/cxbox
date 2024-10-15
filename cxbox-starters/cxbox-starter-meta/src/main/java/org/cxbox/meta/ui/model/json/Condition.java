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

package org.cxbox.meta.ui.model.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Condition {

	private String bcName;

	private IConditionFieldEqualityParams params;
	/**
	 * @deprecated Since 4.0.0-M9 no use
	 */
	@Deprecated(since = "4.0.0-M9")
	private String key;

	/**
	 * @deprecated Since 4.0.0-M9 no use
	 */
	@Deprecated(since = "4.0.0-M9")
	private Long sequence;

	/**
	 * @deprecated Since 4.0.0-M9 no use
	 */
	@Deprecated(since = "4.0.0-M9")
	@JsonProperty(value = "default")
	private Boolean isDefault;

	/**
	 * @deprecated Since 4.0.0-M9 no use
	 */
	@Deprecated(since = "4.0.0-M9")
	private List<IConditionFieldEqualityParams> multipleParams;

	@Getter
	@Setter
	public static class IConditionFieldEqualityParams {

		private String fieldKey;

		private JsonNode value;

		/**
		 * @deprecated Since 4.0.0-M9 no use
		 */
		@Deprecated(since = "4.0.0-M9")
		private String valueList;

	}

}
