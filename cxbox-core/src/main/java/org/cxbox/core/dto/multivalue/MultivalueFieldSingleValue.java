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

package org.cxbox.core.dto.multivalue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonDeserialize(using = MultivalueFieldSingleValueDeserializer.class)
public class MultivalueFieldSingleValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;

	private final String value;

	private Map<String, String> options = new HashMap<>();

	@java.beans.ConstructorProperties({"id", "value", "options"})
	@JsonCreator(mode = Mode.PROPERTIES)
	public MultivalueFieldSingleValue(String id, String value, Map<String, String> options) {
		this.id = id;
		this.value = value;
		this.options = options;
	}

	public MultivalueFieldSingleValue addOption(MultivalueOptionType key, String value) {
		options.put(key.getValue(), value);
		return this;
	}

	public MultivalueFieldSingleValue deleteOption(MultivalueOptionType key) {
		options.remove(key.getValue());
		return this;
	}

}
