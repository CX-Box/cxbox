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

import org.cxbox.api.data.Period;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.controller.param.SearchOperation;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.util.DateTimeUtil;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.cxbox.core.controller.param.SearchOperation.CONTAINS_ONE_OF;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS_ONE_OF;

@Component
@EqualsAndHashCode(callSuper = false)
public class DateValueProvider extends AbstractClassifyDataProvider implements ClassifyDataProvider {

	@Override
	protected List<ClassifyDataParameter> getProviderParameterValues(Field dtoField, ClassifyDataParameter dataParameter,
			FilterParameter filterParam, SearchParameter searchParam,
			List<ClassifyDataProvider> providers) {
		List<ClassifyDataParameter> result = new ArrayList<>();
		if (CONTAINS_ONE_OF.equals(dataParameter.getOperator()) || EQUALS_ONE_OF.equals(dataParameter.getOperator())) {
			boolean tzAware = FieldDTO.isTzAware(dtoField);
			List<Period<LocalDateTime>> periods = new ArrayList<>();
			for (LocalDateTime value : filterParam.getDateValueAsList()) {
				LocalDateTime startValue = value.with(DateTimeUtil.asStartOfDay())
						.with(DateTimeUtil.fromSession(tzAware));
				LocalDateTime endValue = value.with(DateTimeUtil.asEndOfDay()).with(DateTimeUtil.fromSession(tzAware));
				periods.add(new Period<>(startValue, endValue));
			}
			dataParameter.setValue(periods);
			dataParameter.setOperator(SearchOperation.INTERVALS);
		} else {
			setClassifyDateParameterDateValue(dataParameter, filterParam, FieldDTO.isTzAware(dtoField), searchParam, result);
		}
		result.add(dataParameter);
		return result;
	}

}
