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

import org.cxbox.api.data.dictionary.IDictionaryType;
import org.cxbox.api.exception.ServerException;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.dto.LovUtils;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import lombok.EqualsAndHashCode;
import org.cxbox.dictionary.Dictionary;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;
import static org.cxbox.core.controller.param.SearchOperation.CONTAINS_ONE_OF;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS_ONE_OF;

/**
 * @deprecated
 * LOV is deprecated and this Provider too. Instead, use type safe <code>{@link Dictionary}</code> and its provide <code>{@link DictionaryValueProvider}</code>.
 */
@Deprecated(since = "4.0.0-M12", forRemoval = true)
@Component
@EqualsAndHashCode(callSuper = false)
public class LovValueProvider extends AbstractClassifyDataProvider implements ClassifyDataProvider {

	@Override
	protected List<ClassifyDataParameter> getProviderParameterValues(Field dtoField, ClassifyDataParameter dataParameter,
			FilterParameter filterParam, SearchParameter searchParam,
			List<ClassifyDataProvider> providers) {
		List<ClassifyDataParameter> result;
		IDictionaryType type = LovUtils.getType(dtoField);
		if (type == null) {
			throw new ServerException(
					errorMessage("error.missing_lov_annotation", dtoField.getName()));
		}
		if (CONTAINS_ONE_OF.equals(dataParameter.getOperator()) || EQUALS_ONE_OF.equals(dataParameter.getOperator())) {
			dataParameter.setValue(filterParam.getStringValuesAsList().stream()
					.map(type::lookupName)
					.collect(Collectors.toList()));
		} else {
			dataParameter.setValue(type.lookupName(filterParam.getStringValue()));
		}
		result = Collections.singletonList(dataParameter);
		return result;
	}

}
