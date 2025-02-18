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

package org.cxbox.core.config;

import static org.cxbox.dictionary.DictionaryModule.buildDictionaryModule;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import lombok.NonNull;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.util.jackson.DtoPropertyFilter;
import org.cxbox.api.util.jackson.deser.contextual.TZAwareLDTContextualDeserializer;
import org.cxbox.api.util.jackson.ser.contextual.I18NAwareStringContextualSerializer;
import org.cxbox.api.util.jackson.ser.contextual.TZAwareJUDContextualSerializer;
import org.cxbox.api.util.jackson.ser.contextual.TZAwareLDTContextualSerializer;
import org.cxbox.core.config.properties.WidgetFieldsIdResolverProperties;
import org.cxbox.dictionary.Dictionary;
import org.cxbox.dictionary.DictionaryProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.SpringHandlerInstantiator;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableConfigurationProperties(WidgetFieldsIdResolverProperties.class)
public class JacksonConfig {

	@Bean(CxboxBeanProperties.OBJECT_MAPPER)
	public ObjectMapper cxboxObjectMapper(
			HandlerInstantiator handlerInstantiator,
			Optional<DictionaryProvider> dictionaryProvider
	) {
		return Jackson2ObjectMapperBuilder
				.json()
				.handlerInstantiator(handlerInstantiator)
				.modules(buildJavaTimeModule(), i18NModule(), buildDictionaryModule(dictionaryProvider, true))
				.featuresToDisable(
						SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
						SerializationFeature.FLUSH_AFTER_WRITE_VALUE,
						Feature.FLUSH_PASSED_TO_STREAM
				)
				.filters(
						new SimpleFilterProvider().addFilter("dtoPropertyFilter", new DtoPropertyFilter())
				)
				.build();
	}

	@Bean
	public HandlerInstantiator handlerInstantiator(ApplicationContext context) {
		return new SpringHandlerInstantiator(context.getAutowireCapableBeanFactory());
	}

	private JavaTimeModule buildJavaTimeModule() {
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addDeserializer(LocalDateTime.class, new TZAwareLDTContextualDeserializer());
		javaTimeModule.addSerializer(LocalDateTime.class, new TZAwareLDTContextualSerializer());
		javaTimeModule.addSerializer(Date.class, new TZAwareJUDContextualSerializer());
		return javaTimeModule;
	}

	private SimpleModule i18NModule() {
		SimpleModule i18NModule = new SimpleModule();
		i18NModule.addSerializer(String.class, new I18NAwareStringContextualSerializer());
		return i18NModule;
	}

	/**
	 * Inspired with org.springframework.core.convert.support.StringToEnumConverterFactory to force Dictionary be null instead of having null/"" key, when using in Rest params and so on
	 */
	@Bean
	public WebMvcConfigurer dictionaryWebMvcConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addFormatters(@NonNull final FormatterRegistry registry) {
				registry.addConverterFactory(new StringToDictionaryConverterFactory());
			}
		};
	}

	public static class StringToDictionaryConverterFactory implements ConverterFactory<String, Dictionary> {

		@Override
		@NonNull
		public <T extends Dictionary> Converter<String, T> getConverter(@NonNull final Class<T> targetType) {
			if (!Dictionary.class.isAssignableFrom(targetType)) {
				throw new IllegalArgumentException(
						"Target type " + targetType.getName() + " does not refer to dictionary");
			}
			return source -> source.isEmpty() ? null : Dictionary.of(targetType, source);
		}

	}

}
