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

import static org.cxbox.api.util.tz.TimeZoneUtil.toZonedDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import java.io.IOException;
import java.time.LocalDateTime;


public class LDT2ZDTSerializer extends JsonSerializer<LocalDateTime> {

	public static final LDT2ZDTSerializer INSTANCE = new LDT2ZDTSerializer();

	@Override
	public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider prov) throws IOException {
		ZonedDateTimeSerializer.INSTANCE.serialize(toZonedDateTime(value), gen, prov);
	}

}
