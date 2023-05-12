
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

package org.cxbox.api.util.jackson.ser.convert;

import static org.cxbox.api.util.tz.TimeZoneUtil.toLocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.io.IOException;
import java.util.Date;


public class JUD2LDTSerializer extends JsonSerializer<Date> {

	public static final JUD2LDTSerializer INSTANCE = new JUD2LDTSerializer();

	@Override
	public void serialize(Date value, JsonGenerator gen, SerializerProvider prov) throws IOException {
		LocalDateTimeSerializer.INSTANCE.serialize(toLocalDateTime(value), gen, prov);
	}

}
