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

package org.cxbox.dictionary;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Optional;
import org.cxbox.dictionary.key.DictionaryKeyJacksonDeserializer;
import org.cxbox.dictionary.key.DictionaryKeyJacksonSerializer;
import org.cxbox.dictionary.value.DictionaryValueJacksonDeserializer;
import org.cxbox.dictionary.value.DictionaryValueJacksonSerializer;

public class DictionaryModule {

	public static SimpleModule buildDictionaryModule(Optional<DictionaryProvider> dictionaryProvider,
			boolean serializeDeserializeAsValue) {
		if (serializeDeserializeAsValue) {
			return buildDictionaryValueModule(dictionaryProvider);
		} else {
			return buildDictionaryKeyModule();
		}
	}

	private static SimpleModule buildDictionaryValueModule(Optional<DictionaryProvider> dictionaryProvider) {
		SimpleModule module = new SimpleModule("dictionary-datatype");
		module.addSerializer(Dictionary.class, new DictionaryValueJacksonSerializer<>(dictionaryProvider));
		module.setDeserializers(new SimpleDeserializers() {
			//usual module.addDeserializer does support inheritance, so we use findBeanDeserializer to register DictionaryJacksonDeserializer for any inheritor of Dictionary.class
			@Override
			public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config,
					BeanDescription beanDesc) throws JsonMappingException {
				var deserializer = super.findBeanDeserializer(type, config, beanDesc);
				if (deserializer == null && Dictionary.class.isAssignableFrom(type.getRawClass())) {
					return new DictionaryValueJacksonDeserializer<>(dictionaryProvider);
				}
				return deserializer;
			}
		});
		return module;
	}

	private static SimpleModule buildDictionaryKeyModule() {
		SimpleModule module = new SimpleModule("dictionary-datatype");
		module.addSerializer(Dictionary.class, new DictionaryKeyJacksonSerializer<>());
		module.setDeserializers(new SimpleDeserializers() {
			//usual module.addDeserializer does support inheritance, so we use findBeanDeserializer to register DictionaryJacksonDeserializer for any inheritor of Dictionary.class
			@Override
			public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config,
					BeanDescription beanDesc) throws JsonMappingException {
				var deserializer = super.findBeanDeserializer(type, config, beanDesc);
				if (deserializer == null && Dictionary.class.isAssignableFrom(type.getRawClass())) {
					return new DictionaryKeyJacksonDeserializer<>();
				}
				return deserializer;
			}
		});
		return module;
	}


}
