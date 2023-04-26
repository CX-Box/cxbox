/*-
 * #%L
 * IO Cxbox - Core
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

package org.cxbox.core.controller.param.resolvers;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.cxbox.api.data.PageSpecification;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.util.locale.LocaleSpecification;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;


public class LocaleParameterArgumentResolver extends AbstractParameterArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return LocaleSpecification.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		if (!supportsParameter(parameter)) {
			return PageSpecification.DEFAULT;
		}
		return new LocaleSpecification(getLocale(webRequest));
	}


	private LOV getLocale(NativeWebRequest webRequest) {
		String locale = getParameterValue(webRequest.getParameterMap().get("_locale"));

		if (isNotBlank(locale)) {
			return new LOV(locale);
		}

		return null;
	}

}
