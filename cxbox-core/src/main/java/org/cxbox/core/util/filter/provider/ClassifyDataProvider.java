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
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.controller.param.SearchOperation;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.model.core.dao.impl.DialectName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	@Nullable
	default Predicate getFilterPredicate(@NotNull SearchOperation operator, @NotNull Root<?> root, @NotNull CriteriaBuilder cb,
			@NotNull ClassifyDataParameter criteria, @NotNull Path field, @NotNull Object value, @NotNull DialectName dialect) {
		return null;
	}

	@Nullable
	default Expression<?> getSortExpression(@NotNull final SearchParameter searchParameter, @NotNull final CriteriaBuilder builder,
			@Nullable final CriteriaQuery query, @Nullable final Root<?> root, @Nullable final Class dtoClazz, @NotNull Path fieldPath, @Nullable DialectName dialect) {
		return null;
	}

}
