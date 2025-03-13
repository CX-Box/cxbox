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

package org.cxbox.dictionary.key;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cxbox.dictionary.Dictionary;

@RequiredArgsConstructor
public class DictionaryKeyJacksonDeserializer<T extends Dictionary> extends JsonDeserializer<T> implements ContextualDeserializer {

	private Class<T> elementType;

	@Override
	public JsonDeserializer<?> createContextual(final DeserializationContext ctxt, final BeanProperty property) {
		if (ctxt.getContextualType() != null && Dictionary.class.isAssignableFrom(ctxt.getContextualType().getRawClass())) {
			//infer target type from context
			elementType = (Class<T>) ctxt.getContextualType().getRawClass();
		}
		if (elementType == null && property != null) {
			//infer target type from Class field type
			elementType = (Class<T>) property.getType().getRawClass();
		}
		if (elementType == null) {
			throw new IllegalStateException("target class MUST extend " + Dictionary.class.getCanonicalName() + ", but was null");
		}
		return this;
	}

	@Override
	@SneakyThrows
	public T deserialize(final JsonParser p, final DeserializationContext ctxt) {
		final String key = p.readValueAs(String.class);
		return key == null || key.isEmpty() ? null : Dictionary.of(elementType, key);
	}

}