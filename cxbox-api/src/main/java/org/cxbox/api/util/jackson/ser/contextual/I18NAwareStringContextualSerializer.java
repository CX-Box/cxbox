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

package org.cxbox.api.util.jackson.ser.contextual;

import org.cxbox.api.data.dto.LocaleAware;
import org.cxbox.api.util.jackson.ser.contextaware.I18NAwareStringSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import java.io.IOException;


public class I18NAwareStringContextualSerializer extends JsonSerializer<String> implements ContextualSerializer {

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider prov) throws IOException {
		if (isLocaleAware(gen)) {
			I18NAwareStringSerializer.INSTANCE.serialize(value, gen, prov);
		} else {
			I18NAwareStringSerializer.DELEGATE.serialize(value, gen, prov);
		}
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
		if (isLocaleAware(property)) {
			return I18NAwareStringSerializer.INSTANCE;
		}
		return this;
	}

	private boolean isLocaleAware(JsonGenerator gen) {
		return false;
	}

	private boolean isLocaleAware(BeanProperty property) {
		if (property == null) {
			return false;
		}
		LocaleAware annotation = property.getAnnotation(LocaleAware.class);
		return annotation != null;
	}

}
