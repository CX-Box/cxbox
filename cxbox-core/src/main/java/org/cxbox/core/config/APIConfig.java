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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.core.config.properties.APIProperties;
import org.cxbox.core.controller.param.resolvers.LocaleParameterArgumentResolver;
import org.cxbox.core.controller.param.resolvers.PageParameterArgumentResolver;
import org.cxbox.core.controller.param.resolvers.QueryParametersResolver;
import org.cxbox.core.controller.param.resolvers.TimeZoneParameterArgumentResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@ControllerScan({"org.cxbox.core.controller"})
@AllArgsConstructor
@EnableConfigurationProperties(APIProperties.class)
public class APIConfig implements WebMvcConfigurer {

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	protected final ObjectMapper objectMapper;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new PageParameterArgumentResolver());
		argumentResolvers.add(new QueryParametersResolver());
		argumentResolvers.add(new TimeZoneParameterArgumentResolver());
		argumentResolvers.add(new LocaleParameterArgumentResolver());
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new StringHttpMessageConverter());
		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
		converters.add(new ResourceHttpMessageConverter());
		converters.add(new ResourceRegionHttpMessageConverter());
	}

	@SuppressWarnings("java:S5693")
	@Bean
	@ConditionalOnProperty(value = "cxbox.bean.multipart-resolver.enabled", matchIfMissing = true)
	public MultipartResolver multipartResolver() {
		StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
		/*resolver.setMaxUploadSize(268435456L);
		resolver.setDefaultEncoding(StandardCharsets.UTF_8.name());*/
		return resolver;
	}



}
