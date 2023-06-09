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

import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.dto.multivalue.MultivalueField;
import org.cxbox.core.exception.ClientException;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;
import static org.cxbox.core.controller.param.SearchOperation.CONTAINS_ONE_OF;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS_ONE_OF;

@Component
@EqualsAndHashCode(callSuper = false)
public class MultiFieldValueProvider extends AbstractClassifyDataProvider implements ClassifyDataProvider {

	@Override
	protected List<ClassifyDataParameter> getProviderParameterValues(Field dtoField, ClassifyDataParameter dataParameter,
			FilterParameter filterParam, SearchParameter searchParam,
			List<ClassifyDataProvider> providers) {
		String fieldName = dtoField.getDeclaredAnnotation(SearchParameter.class).name();
		Class<? extends ClassifyDataProvider> keyProvider = dtoField.getDeclaredAnnotation(SearchParameter.class).multiFieldKey();
		if (!fieldName.isEmpty() && (CONTAINS_ONE_OF.equals(dataParameter.getOperator()) || EQUALS_ONE_OF.equals(dataParameter.getOperator()))) {
			dataParameter.setField(fieldName);
			ClassifyDataProvider provider = providers.stream().filter(p -> p.getClass().equals(keyProvider))
					.findFirst().orElseThrow(() -> new ClientException(errorMessage("error.data_provider_not_found")));
			provider.getClassifyDataParameters(dtoField, filterParam, searchParam, providers).stream().findFirst().ifPresent(
					classifyDataParameter -> dataParameter.setValue(classifyDataParameter.getValue())
			);
			return Collections.singletonList(dataParameter);
		} else {
			throw new ClientException(errorMessage("error.unsupported_type_filtration", MultivalueField.class));
		}
	}

}
