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

package org.cxbox.api.data.dto.rowmeta;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
class FieldsDTOCurrentValueSerializer extends JsonSerializer<Object> {

	private final ApplicationContext applicationContext;

	@Override
	public void serialize(Object toSerialize, JsonGenerator jgen, SerializerProvider provider)
			throws IOException {
		if (jgen.getOutputContext().getCurrentValue() instanceof FieldDTO fieldDTO
				&& fieldDTO.hasSerializerAnnotation()) {
			Class<? extends JsonSerializer> fieldLevelSerializer = fieldDTO.getFieldLevelSerializer();
			var jsonSerializer = getSerializerFromSerializerAnnotation(fieldLevelSerializer);
			var serialize = Arrays.stream(fieldLevelSerializer.getMethods())
					.filter(method -> method.getName().equals("serialize"))
					.map(method -> method.getParameterTypes()[0])
					.filter(element -> element.isInstance(toSerialize))
					.map(element -> element.cast(toSerialize))
					.findFirst().orElse(null);
			if (serialize != null && jsonSerializer != null) {
				jsonSerializer.serialize(serialize, jgen, provider);
			} else {
				jgen.writeObject(toSerialize);
			}
		} else {
			jgen.writeObject(toSerialize);
		}
	}

	private JsonSerializer<Object> getSerializerFromSerializerAnnotation(
			Class<? extends JsonSerializer> fieldLevelSerializer) {
		try {
			return applicationContext.getBean(fieldLevelSerializer);
		} catch (Exception e) {
			try {
				return fieldLevelSerializer.getDeclaredConstructor().newInstance();
			} catch (Exception ex) {
				log.error("Error getting a JsonSerializer instance. Exception message: " + e.getMessage());
				return null;
			}
		}
	}

}
