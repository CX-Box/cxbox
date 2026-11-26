/*
 * © OOO "SI IKS LAB", 2022-2026
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

package org.cxbox.core.service;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Collects the fields with a wrong value format when a DTO factory reads a form. Other reads of the mapper get the
 * usual Jackson error.
 * <pre>{@code
 * mapper.addHandler(DtoDeserializationHandler.INSTANCE); // the same instance is added only once
 * Set<String> badFields = new HashSet<>();
 * Object dto = DtoDeserializationHandler.convert(mapper, form, dtoClass, badFields);
 * }</pre>
 * The handler stays in the shared mapper: Jackson 2 cannot give a handler to one {@code convertValue}. The full fix
 * will come only with Jackson 3: <a href="https://github.com/FasterXML/jackson-databind/issues/1196">jackson-databind#1196</a>,
 * <a href="https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.1">Jackson 3.1</a>.
 */
final class DtoDeserializationHandler extends DeserializationProblemHandler {

	static final DtoDeserializationHandler INSTANCE = new DtoDeserializationHandler();

	private static final ThreadLocal<Set<String>> CURRENT = new ThreadLocal<>();

	private DtoDeserializationHandler() {
	}

	/**
	 * Converts the form with a plain {@code convertValue}; fields with a wrong value format go to {@code badFields}.
	 * The mapper must have {@link #INSTANCE}. {@code badFields} sits in a {@link ThreadLocal} only while this method
	 * runs; a deserializer that reads on another thread is not supported.
	 */
	static Object convert(ObjectMapper mapper, Map<String, Object> form, Class<?> clazz, Set<String> badFields) {
		CURRENT.set(badFields);
		try {
			return mapper.convertValue(form, clazz);
		} finally {
			CURRENT.remove();
		}
	}

	@Override
	public Object handleWeirdStringValue(
			DeserializationContext ctxt,
			Class<?> targetType,
			String valueToConvert,
			String failureMsg) {
		return add(ctxt) ? null : NOT_HANDLED;
	}

	@Override
	public Object handleWeirdNumberValue(
			DeserializationContext ctxt,
			Class<?> targetType,
			Number valueToConvert,
			String failureMsg) {
		return add(ctxt) ? null : NOT_HANDLED;
	}

	@Override
	public JavaType handleUnknownTypeId(
			DeserializationContext ctxt,
			JavaType baseType,
			String subTypeId,
			TypeIdResolver idResolver,
			String failureMsg) {
		add(ctxt);
		return null;
	}

	/**
	 * @deprecated since 5.0.4, use {@link #convert(ObjectMapper, Map, Class, Set)}
	 */
	@Deprecated(since = "5.0.4", forRemoval = true)
	public Set<String> getFields() {
		return Collections.emptySet();
	}

	private static boolean add(DeserializationContext ctxt) {
		Set<String> errors = CURRENT.get();
		if (errors == null) {
			return false;
		}
		errors.add(ctxt.getParser().getParsingContext().getCurrentName());
		return true;
	}

}
