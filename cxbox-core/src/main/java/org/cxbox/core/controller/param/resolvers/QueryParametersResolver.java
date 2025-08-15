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

package org.cxbox.core.controller.param.resolvers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.dto.rowmeta.jackson.StringOrArrayToStringDeserializer;
import org.cxbox.core.controller.filter.CachedBodyHttpServletRequest;
import org.cxbox.core.controller.param.QueryParameters;
import org.springframework.core.MethodParameter;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;


/**
 * Resolves {@link QueryParameters} from both standard query parameters and
 * JSON request bodies. Supports multiple reads of the request body via
 * {@link CachedBodyHttpServletRequest}.
 * <p>
 * When the controller method declares a {@code QueryParameters} argument,
 * this resolver will:
 * <ol>
 *   <li>Collect all HTTP query parameters into a map.</li>
 *   <li>If the request is non-GET and wrapped by {@link CachedBodyHttpServletRequest},
 *       parse the JSON body, extract an array node named <code>filter</code>,
 *       convert each element into a {@link FilterDTO}, then merge into the map.</li>
 *   <li>Return a {@link QueryParameters} instance populated from that map.</li>
 * </ol>
 * </p>
 *
 * <p><strong>Note:</strong> To enable multiple reads of the JSON body,
 * a {@link CachedBodyHttpServletRequest} must be installed (e.g., via a filter)
 * before this resolver runs.</p>
 *
 * @see QueryParameters
 * @see CachedBodyHttpServletRequest
 */
@RequiredArgsConstructor
@Slf4j
public class QueryParametersResolver extends AbstractParameterArgumentResolver {

	private final ObjectMapper objectMapper;

	/**
	 * Supports only {@link QueryParameters} parameters.
	 *
	 * @param parameter the method parameter
	 * @return {@code true} if the parameter type is {@link QueryParameters}
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return QueryParameters.class.equals(parameter.getParameterType());
	}

	/**
	 * Resolves the {@link QueryParameters} value by combining URL parameters
	 * and JSON body filters (if present).
	 *
	 * @param parameter     the method parameter
	 * @param mavContainer  the model and view container (unused)
	 * @param webRequest    the current request context
	 * @param binderFactory the data binder factory (unused)
	 * @return a populated {@link QueryParameters} instance
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		if (!supportsParameter(parameter)) {
			return QueryParameters.emptyQueryParameters();
		}
		HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		Map<String, String> parameters = webRequest.getParameterMap().entrySet().stream()
				.filter(Objects::nonNull)
				.filter(entry -> entry.getValue() != null)
				.filter(entry -> entry.getValue().length > 0)
				.collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue()[0]));

		if (nativeRequest instanceof final CachedBodyHttpServletRequest cachedBodyHttpServletRequest && !"GET".equals(cachedBodyHttpServletRequest.getMethod())) {
			try {
					JsonNode rootNode = objectMapper.readTree(cachedBodyHttpServletRequest.getInputStream());
					JsonNode filterNode = rootNode.get("filter");
					List<FilterDTO> filters =
							filterNode != null && filterNode.isArray()
									? objectMapper.readerForListOf(FilterDTO.class).readValue(filterNode)
									: Collections.emptyList();
					filters.stream()
							.map(FilterDTO::toQueryParams)
							.forEach(f -> parameters.put(f.getFirst(), f.getSecond()));

			} catch (IOException e) {
				log.warn("Cannot resolve parameters from post  {}: {}", e.getMessage(), e.getStackTrace());
			}
		}
		return new QueryParameters(parameters);
	}

	/**
	 * DTO representing an individual filter entry in the JSON body.
	 */
	@Data
	public static class FilterDTO {

		private String fieldName;

		private String type;

		/**
		 * The filter values, deserialized from either a JSON string or array.
		 * Custom deserializer {@link StringOrArrayToStringDeserializer} applied.
		 */
		@JsonDeserialize(using = StringOrArrayToStringDeserializer.class)
		private String value;

		/**
		 * Converts this DTO into a query-parameter pair:
		 * {@code "<fieldName>.<type>" -> values}.
		 *
		 * @return the name/value pair for query parameters
		 */
		public Pair<String, String> toQueryParams() {
			return Pair.of(fieldName + "." + type, value);
		}

	}

}
