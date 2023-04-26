/*-
 * #%L
 * IO Cxbox - API
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.api.data.dto.rowmeta;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

class FieldsDTOSerializer extends JsonSerializer<FieldsDTO> {

	@Override
	public void serialize(FieldsDTO toSerialize, JsonGenerator jgen, SerializerProvider provider)
			throws IOException {
		jgen.writeStartArray();
		for (FieldDTO dto : toSerialize) {
			jgen.writeObject(dto);
		}
		jgen.writeEndArray();
	}

}
