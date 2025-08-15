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
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.VirtualAnnotatedMember;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
class FieldsDTOCurrentValueSerializer extends JsonSerializer<Object> {

	private final BeanFactory beanFactory;

	@Override
	public void serialize(Object toSerialize, JsonGenerator jgen, SerializerProvider provider)
			throws IOException {
		if (jgen != null && jgen.getOutputContext() != null &&
				jgen.getOutputContext().getCurrentValue() instanceof FieldDTO fieldDTO
				&& fieldDTO.hasSerializerAnnotation()) {
			Class<? extends JsonSerializer> fieldLevelSerializer = fieldDTO.getFieldLevelSerializer();
			JsonSerializer<Object> jsonSerializer = null;
			if (fieldDTO.hasSerializerAnnotation()) {
				jsonSerializer = getObjectJsonSerializer(toSerialize, provider, fieldDTO);
			}
			if (jsonSerializer == null) {
				jsonSerializer = getSerializerFromSerializerAnnotation(fieldLevelSerializer);
			}
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

	/**
	 * Creates and returns a {@link JsonSerializer} for the provided object and field definition,
	 * resolving Jackson serialization context when necessary.
	 * <p>
	 * This method performs the following steps:
	 * <ol>
	 *   <li>Constructs a {@link JavaType} for the given {@code toSerialize} object's runtime class.</li>
	 *   <li>Creates a virtual annotated member ({@link VirtualAnnotatedMember}) representing
	 *       the JSON property described by {@link FieldDTO#getKey()} and the target type.</li>
	 *   <li>Obtains a serializer class for that member using the serializer class (if any)
	 *       defined in {@link FieldDTO#getFieldLevelSerializer()} via
	 *       {@link SerializerProvider#serializerInstance(com.fasterxml.jackson.databind.introspect.Annotated, Object)}.</li>
	 *   <li>If the serializer implements {@link ContextualSerializer}, invokes its
	 *       {@link ContextualSerializer#createContextual(SerializerProvider, BeanProperty)} method,
	 *       passing a {@link BeanProperty} built from a {@link SimpleBeanPropertyDefinition}
	 *       derived from the member, to customize serialization behavior for this specific property context.</li>
	 * </ol>
	 *
	 * <b>Note:</b> In this implementation, the {@code typeContext} parameter for
	 * {@link VirtualAnnotatedMember} is passed as {@code null}.
	 * If precise generic type resolution is required, consider constructing an appropriate
	 * {@link com.fasterxml.jackson.databind.introspect.TypeResolutionContext}.
	 *
	 * <p>If a {@link JsonMappingException} occurs during serializer creation or contextualization,
	 * it is caught, a warning is logged, and {@code null} is returned.</p>
	 *
	 * @param toSerialize the Java object instance for which a serializer should be created
	 * (must not be {@code null}).
	 * @param provider the active {@link SerializerProvider} that can construct types and
	 * resolve serializer instances from Jackson's configuration.
	 * @param fieldDTO metadata describing the field to be serialized, including its logical
	 * JSON property name and optional custom serializer class.
	 * @return a {@link JsonSerializer} instance capable of serializing the given object
	 * according to the provided {@link FieldDTO}, possibly contextualized;
	 * or {@code null} if serializer creation fails.
	 * @see VirtualAnnotatedMember
	 * @see JsonSerializer
	 * @see ContextualSerializer
	 * @see SerializerProvider#serializerInstance(com.fasterxml.jackson.databind.introspect.Annotated, Object)
	 */

	private static JsonSerializer<Object> getObjectJsonSerializer(@NonNull Object toSerialize,
			@NonNull SerializerProvider provider,
			@NonNull FieldDTO fieldDTO) {
		JsonSerializer<Object> jsonSerializer = null;
		try {
			JavaType type = provider.constructType(toSerialize.getClass());
			AnnotatedMember member = new VirtualAnnotatedMember(null, type.getRawClass(), fieldDTO.getKey(), type);
			jsonSerializer = provider.serializerInstance(
					member,
					fieldDTO.getFieldLevelSerializer()
			);

			if (jsonSerializer instanceof ContextualSerializer) {
				SimpleBeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(
						provider.getConfig(),
						member
				);
				BeanProperty prop = new BeanProperty.Std(
						propDef.getFullName(),
						type,
						propDef.getWrapperName(),
						member,
						PropertyMetadata.STD_OPTIONAL
				);
				jsonSerializer = (JsonSerializer<Object>) ((ContextualSerializer) jsonSerializer)
						.createContextual(provider, prop);
			}
			return jsonSerializer;
		} catch (JsonMappingException e) {
			log.warn("Error in getting instance provider from jackson {}", e);
		}
		return null;
	}

	private JsonSerializer<Object> getSerializerFromSerializerAnnotation(
			@NonNull Class<? extends JsonSerializer> fieldLevelSerializer) {

		if (beanFactory.containsBean(fieldLevelSerializer.getName())) {
			return beanFactory.getBean(fieldLevelSerializer);
		}

		if (fieldLevelSerializer.getDeclaredConstructors().length > 0) {
			Optional<Constructor<?>> optConstructorDefaultConstructor = Arrays.stream(fieldLevelSerializer.getDeclaredConstructors())
					.filter(FieldsDTOCurrentValueSerializer::isZeroArgConstructorInPublicClassAccessible).findFirst();
			if (optConstructorDefaultConstructor.isPresent()) {
				try {
					return fieldLevelSerializer.getDeclaredConstructor().newInstance();
				} catch (Exception e) {
					log.warn(
							"Cannot getting a {} instance with constructor without parameters. Exception message: {}",
							fieldLevelSerializer.getName(),
							e.getMessage()
					);
				}
			}
		}
		log.warn(
				"Cannot find bean {} or class with constructor without parameters. Please register {} as bean",
				fieldLevelSerializer.getName(),
				fieldLevelSerializer.getName()
		);
		return null;
	}

	/**
	 * Checks whether the given {@link Constructor} has no parameters and is accessible as public.
	 * <p>
	 * A constructor is considered matching this condition if:
	 * <ul>
	 *   <li>It has zero parameters ({@code getParameterCount() == 0}).</li>
	 *   <li>It is declared as {@code public}</li>
	 *   <li>It is marked as accessible according to {@link Constructor#canAccess}.</li>
	 * </ul>
	 *
	 * @param constructor the {@link Constructor} instance to check; must not be {@code null}.
	 * @return {@code true} if the constructor meets all conditions (no parameters, public, accessible),
	 * {@code false} otherwise.
	 */
	private static boolean isZeroArgConstructorInPublicClassAccessible(@NonNull Constructor<?> constructor) {
		return constructor.getParameterCount() == 0 && Modifier.isPublic(constructor.getModifiers())
				&& constructor.canAccess(null);
	}

}
