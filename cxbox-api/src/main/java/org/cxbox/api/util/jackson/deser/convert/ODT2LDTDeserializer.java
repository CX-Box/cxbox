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

package org.cxbox.api.util.jackson.deser.convert;

import static org.cxbox.api.util.tz.TimeZoneUtil.toLocalDateTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;


public class ODT2LDTDeserializer extends JsonDeserializer<LocalDateTime> {

	public static final ODT2LDTDeserializer INSTANCE = new ODT2LDTDeserializer();

	@Override
	public LocalDateTime deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
		OffsetDateTime result = InstantDeserializer.OFFSET_DATE_TIME.deserialize(parser, ctx);
		if (result == null) {
			return null;
		}
		return toLocalDateTime(result);
	}

}
