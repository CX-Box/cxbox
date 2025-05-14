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
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
import org.cxbox.core.controller.param.SearchOperation;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.dao.impl.MetadataUtils;
import org.cxbox.core.exception.ClientException;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import org.cxbox.model.core.dao.impl.DialectName;
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

	/**
	 * Sorts entity column by time fraction (LocalDateTime and LocalTime)
	 * <br>
	 * {@code dialect (Oracle/PostgreSQL)} - entityManager.getEntityManagerFactory().getProperties().get("hibernate.dialect").toString();
	 * <br>
	 * <h6>dialect = PostgreSQL</h6>
	 * Column in DB: TIMESTAMP/TIME
	 * <br>
	 * Actual SQL:
	 * <br>
	 * {@code Query:["select a1_0.id,a1_0.commend,a1_0.created_date from apple a1_0 order by cast(a1_0.created_date as time) asc"]}
	 * so always add functional index CREATE INDEX idx_apple ON my_entity (cast(field as time(6)));
	 * <br>
	 * <h6>dialect = Oracle</h6>
	 * Column in DB: TIMESTAMP
	 * <br>
	 * Actual SQL:
	 * {@code Query:["select a1_0.id,a1_0.commend,a1_0.created_date from apple a1_0 order by to_char(a1_0.created_date,'HH24:MI:SS') asc"]}
	 * <br>
	 */
	@Nullable
	public Expression<?> getSortExpression(@NonNull final SearchParameter searchParameter, @NonNull final CriteriaBuilder builder,
			@NonNull final CriteriaQuery query, @NonNull final Root<?> root, @NonNull final Class dtoClazz,  @NonNull Path fieldPath, @NonNull DialectName dialect) {
		if (searchParameter.provider() != null &&
				searchParameter.provider().equals(TimeValueProvider.class)) {
			if (dialect.equals(DialectName.ORACLE)) {
				return builder.function("TO_CHAR", String.class, fieldPath, builder.literal("HH24:MI:SS"));
			}
			return fieldPath.as(LocalTime.class);
		}
		return null;
	}

	@Nullable
	public Predicate getFilterPredicate(@NonNull SearchOperation operator, @NonNull Root<?> root,
			@NonNull CriteriaBuilder cb,
			@NonNull ClassifyDataParameter criteria, @NonNull Path field,  @NonNull Object value, @NonNull DialectName dialect) {
		if (value instanceof LocalTime) {
			switch (operator) {
				case EQUALS:
					return cb.equal(getExpressionByTimePart(field, cb, dialect), MetadataUtils.requireComparable(value));
				case GREATER_OR_EQUAL_THAN:
					return cb.greaterThanOrEqualTo(
							getExpressionByTimePart(field, cb, dialect),
							MetadataUtils.requireComparable(value)
					);
				case LESS_OR_EQUAL_THAN:
					return cb.lessThanOrEqualTo(
							getExpressionByTimePart(field, cb, dialect),
							MetadataUtils.requireComparable(value)
					);
				default:
					return null;
			}
		}
		return null;
	}


	@NonNull
	private static Expression getExpressionByTimePart(@NonNull Path field, @NonNull CriteriaBuilder cb, @NonNull DialectName dialect) {
		if (dialect.equals(DialectName.ORACLE)) {
			return cb.function("TO_CHAR", String.class, field, cb.literal("HH24:MI:SS"));

		}
		return field.as(LocalTime.class);
	}

}
