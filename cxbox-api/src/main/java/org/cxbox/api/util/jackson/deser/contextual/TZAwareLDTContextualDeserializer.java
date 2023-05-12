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

package org.cxbox.api.util.jackson.deser.contextual;

import static org.cxbox.api.util.tz.TimeZoneUtil.isTzAware;

import org.cxbox.api.util.jackson.deser.contextaware.TZAwareLDTDeserializer;
import org.cxbox.api.util.jackson.deser.convert.LDTDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;


public class TZAwareLDTContextualDeserializer extends JsonDeserializer<LocalDateTime> implements
		ContextualDeserializer {

	/**
	 *
	 * @param parser Parsed used for reading JSON content
	 * @param ctx Context that can be used to access information about
	 *   this deserialization activity.
	 *
	 * @return LDTDeserializer.INSTANCE.deserialize(parser, ctx). One can override this method and return TZAwareLDTDeserializer.INSTANCE.deserialize(parser, ctx) or for TimeZoneAware Deserialization
	 * @throws IOException
	 */
	@Override
	public LocalDateTime deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
		return LDTDeserializer.INSTANCE.deserialize(parser, ctx);
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) {
		if (isTzAware(property)) {
			return TZAwareLDTDeserializer.INSTANCE;
		}
		return LDTDeserializer.INSTANCE;
	}

}
