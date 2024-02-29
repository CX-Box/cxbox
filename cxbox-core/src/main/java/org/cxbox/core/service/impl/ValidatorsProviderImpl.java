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

package org.cxbox.core.service.impl;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import org.cxbox.core.service.ValidatorsProvider;
import java.util.Locale;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.stereotype.Component;


@Component
public class ValidatorsProviderImpl implements ValidatorsProvider {

	private Validator validator;

	public ValidatorsProviderImpl() {
		if (this.validator != null) {
			return;
		}
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator(new MessageInterpolator() {
					@Override
					public String interpolate(String s, Context context) {
						return s.startsWith("{") && s.endsWith("}")
								? errorMessage(s.substring(1, s.length() - 1))
								: s;
					}

					@Override
					public String interpolate(String s, Context context, Locale locale) {
						return s.startsWith("{") && s.endsWith("}")
								? errorMessage(s.substring(1, s.length() - 1))
								: s;
					}
				})
				.buildValidatorFactory();
		this.validator = validatorFactory.getValidator();
	}

	@Override
	public Validator getValidator(Class<?> clazz) {
		return this.validator;
	}

}
