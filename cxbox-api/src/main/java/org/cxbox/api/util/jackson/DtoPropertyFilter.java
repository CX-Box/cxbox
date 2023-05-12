
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

package org.cxbox.api.util.jackson;

import org.cxbox.api.data.dto.DataResponseDTO;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;


public class DtoPropertyFilter extends SimpleBeanPropertyFilter {

	@Override
	public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider prov, PropertyWriter writer)
			throws Exception {
		if (!needSerialize(pojo, writer)) {
			return;
		}
		writer.serializeAsField(pojo, jgen, prov);
	}

	private boolean needSerialize(Object pojo, PropertyWriter writer) {
		if (pojo instanceof DataResponseDTO) {
			DataResponseDTO dto = (DataResponseDTO) pojo;
			return dto.isFieldSerializable(writer.getName());
		}
		return true;
	}

}
