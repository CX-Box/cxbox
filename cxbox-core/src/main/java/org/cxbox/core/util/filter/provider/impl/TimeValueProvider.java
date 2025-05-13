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
import static org.cxbox.core.controller.param.SearchOperation.CONTAINS_ONE_OF;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS_ONE_OF;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.exception.ClientException;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import org.springframework.stereotype.Component;

@Component
@EqualsAndHashCode(callSuper = false)
public class TimeValueProvider extends AbstractClassifyDataProvider implements ClassifyDataProvider {

	@Override
	protected List<ClassifyDataParameter> getProviderParameterValues(Field dtoField, ClassifyDataParameter dataParameter,
			FilterParameter filterParam, SearchParameter searchParam,
			List<ClassifyDataProvider> providers) {
		List<ClassifyDataParameter> result = new ArrayList<>();
		if (CONTAINS_ONE_OF.equals(dataParameter.getOperator()) || EQUALS_ONE_OF.equals(dataParameter.getOperator())) {
			throw new ClientException(errorMessage("error.unsupported_type_filtration", LocalDateTime.class));
		}
		dataParameter.setValue(filterParam.getTimeValue());
		result = Collections.singletonList(dataParameter);
		return result;
	}

	@Nullable
	public static Expression<?> getOrder(SearchParameter searchParameter, String dialect, Path fieldPath,
			CriteriaBuilder builder) {
		if (searchParameter != null &&
				searchParameter.provider() != null &&
				searchParameter.provider().equals(TimeValueProvider.class)) {
			return getExpressionByTimePart(dialect, fieldPath, builder);
		}
		return null;
	}

	@NonNull
	public Expression getFilterPredicate(CriteriaBuilder cb,
			ClassifyDataParameter criteria, Path field, String dialect, Object value) {
		if (TimeValueProvider.class.equals(criteria.getProvider()) && value instanceof LocalTime valueLT) {
			return
					getExpressionByTimePart(dialect, field, cb);
		}
		return null;
	}


	private static Expression getExpressionByTimePart(String dialect, Path field, CriteriaBuilder cb) {
		if (dialect.contains("Oracle")) {
			return cb.function("TO_CHAR", String.class, field, cb.literal("HH24:MI:SS"));

		}
		return field.as(LocalTime.class);
	}

}
