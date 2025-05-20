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

package org.cxbox.core.util.filter.provider;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.List;
import lombok.NonNull;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.controller.param.SearchOperation;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.model.core.dao.impl.DialectName;

public interface ClassifyDataProvider {

	/**
	 * @param dtoField DTO field to search or sort by
	 * @param filterParam Filter parameter that defines the type of operation and the values to filter
	 * @param searchParam Search parameter annotation that provides search meta information
	 * @param providers all Classify Data Providers providing Classify Data Parameters
	 * @return Classify Data Parameters for defining of predicates of searching data in Persistence Layer
	 */
	List<ClassifyDataParameter> getClassifyDataParameters(Field dtoField, FilterParameter filterParam,
			SearchParameter searchParam, List<ClassifyDataProvider> providers);

	/**
	 * Retrieves a filter predicate based on the provided search operation and criteria.
	 *
	 * <p>This method is intended to be overridden in the implementing class (Provider)
	 * if custom filtering logic is required. If this method is not implemented,
	 * the default filtering mechanism will be used.</p>
	 */
	default Predicate getFilterPredicate(@NonNull SearchOperation operator, @NonNull Root<?> root, @NonNull CriteriaBuilder cb,
			@NonNull ClassifyDataParameter criteria, @NonNull Path field, @NonNull Object value, @NonNull DialectName dialect) {
		return null;
	}

	/**
	 * Retrieves a sort expression based on the provided search parameter and criteria.
	 *
	 * <p>This method is intended to be overridden in the implementing class (Provider)
	 * if custom sorting logic is required. If this method is not implemented,
	 * the default sorting mechanism will be used.</p>
	 *
	 */
	default Expression<?> getSortExpression(@NonNull final SearchParameter searchParameter, @NonNull final CriteriaBuilder builder,
			@NonNull final CriteriaQuery query, @NonNull final Root<?> root, @NonNull final Class dtoClazz, @NonNull Path fieldPath, @NonNull DialectName dialect) {
		return null;
	}

}
