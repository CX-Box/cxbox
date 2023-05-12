
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

import org.cxbox.core.controller.param.QueryParameters;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;


public class QueryParametersResolver extends AbstractParameterArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return QueryParameters.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		if (!supportsParameter(parameter)) {
			return QueryParameters.emptyQueryParameters();
		}
		return new QueryParameters(
				webRequest.getParameterMap().entrySet().stream()
						.filter(Objects::nonNull)
						.filter(entry -> entry.getValue() != null)
						.filter(entry -> entry.getValue().length > 0)
						.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]))
		);
	}

}
