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

package org.cxbox.core.util.filter.provider.impl;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.exception.ClientException;
import org.cxbox.core.util.filter.MultisourceSearchParameter;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import org.springframework.stereotype.Component;

@Component
@EqualsAndHashCode
public class MultisourceValueProvider implements ClassifyDataProvider {

	@Override
	public List<ClassifyDataParameter> getClassifyDataParameters(Field dtoField, FilterParameter filterParam,
			SearchParameter searchParam, List<ClassifyDataProvider> providers) {
		MultisourceSearchParameter multisourceParameter = dtoField
				.getDeclaredAnnotation(MultisourceSearchParameter.class);
		ClassifyDataParameter cdParameter = ClassifyDataParameter
				.builder()
				.field(filterParam.getName())
				.operator(filterParam.getOperation())
				.provider(MultisourceValueProvider.class)
				.value(Stream.of(multisourceParameter.value())
						.map(par -> {
							ClassifyDataProvider provider = providers.stream().filter(p -> p.getClass().equals(par.provider()))
									.findFirst().orElseThrow(() -> new ClientException(errorMessage("error.data_provider_not_found")));
							return provider.getClassifyDataParameters(dtoField, filterParam, par, providers);
						})
						.filter(par -> !par.isEmpty())
						.collect(Collectors.toList()))
				.build();
		return Collections.singletonList(cdParameter);

	}

}
