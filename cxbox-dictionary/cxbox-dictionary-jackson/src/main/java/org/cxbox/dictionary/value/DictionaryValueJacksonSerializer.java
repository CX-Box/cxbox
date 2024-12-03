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

package org.cxbox.dictionary.value;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cxbox.dictionary.Dictionary;
import org.cxbox.dictionary.DictionaryProvider;
import org.cxbox.dictionary.DictionaryProvider.DictionaryValue;

@RequiredArgsConstructor
public class DictionaryValueJacksonSerializer<T extends Dictionary> extends JsonSerializer<T> {

	private final Optional<DictionaryProvider> dictionaryProvider;

	@Override
	public void serialize(T dictionary, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		String value = dictionaryProvider
				.map(e -> {
					var dict = e.lookupValue(dictionary);
					return Optional.ofNullable(dict).map(DictionaryValue::getValue).orElse(null);
				})
				.filter(Objects::nonNull)
				.orElse(dictionary.key());
		gen.writeString(value);
	}
}
