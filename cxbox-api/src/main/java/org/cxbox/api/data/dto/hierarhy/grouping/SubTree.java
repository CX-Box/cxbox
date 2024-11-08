/*
 * © OOO "SI IKS LAB", 2022-2024
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

package org.cxbox.api.data.dto.hierarhy.grouping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@EqualsAndHashCode(of = {"value"})
@Accessors(chain = true)
public class SubTree<T, G extends SubTree<?, ?>> implements Serializable {

	@JsonInclude
	private T value;

	@JsonInclude
	private Set<G> child;

	@JsonInclude
	private Map<String, String> options;

	@JsonInclude
	private Boolean defaultExpanded;

	@JsonIgnore
	SubTree(T value, Set<G> child, Map<String, String> options, Boolean defaultExpanded) {
		this.value = value;
		this.child = child;
		this.options = options;
		this.defaultExpanded = defaultExpanded;
	}

}
