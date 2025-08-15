/*
 * © OOO "SI IKS LAB", 2022-2025
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

package org.cxbox.api.data.dto.rowmeta.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

/**
 * A custom Jackson deserializer that converts a JSON value—either a single element or an array—
 * into a {@link String}.
 * <p>
 * If the incoming JSON node is an array, the entire array is serialized back to its JSON string
 * representation (e.g., <code>["a","b"]</code>). If it is a scalar value, the node’s text content
 * is returned (e.g., <code>"a"</code>).</p>
 *
 * <p>This is useful when a JSON property may be variably provided as either a single value or
 * an array, but the application logic expects a unified {@code String} representation.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * public class FilterDTO {
 *     private String fieldname;
 *     private String operation;
 *
 *     @JsonDeserialize(using = StringOrArrayToStringDeserializer.class)
 *     private String values;
 *
 *     // getters/setters...
 * }
 * }</pre>
 *
 * <p><strong>Example JSON:</strong></p>
 * <ul>
 *   <li><code>{ "values": "one" }</code> deserializes to <code>"one"</code></li>
 *   <li><code>{ "values": ["one","two"] }</code> deserializes to <code>"[\"one\",\"two\"]"</code></li>
 * </ul>
 *
 */
public class StringOrArrayToStringDeserializer extends JsonDeserializer<String> {

	/**
	 * Deserializes JSON content into a {@link String}, handling both scalar and array nodes.
	 *
	 * @param p     the {@link JsonParser} pointing to the value to deserialize
	 * @param ctxt  the Jackson context for deserialization
	 * @return      the JSON array as a string if the node is an array, otherwise the node’s text
	 * @throws IOException if an I/O error occurs while reading the JSON
	 */
	@Override
	public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonNode node = p.readValueAsTree();
		if (node.isArray()) {
			return node.toString();
		} else {
			return node.asText();
		}
	}

}
