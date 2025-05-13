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
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import java.lang.reflect.Field;
import java.util.List;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.util.filter.SearchParameter;

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


	default Expression getFilterPredicate(CriteriaBuilder cb, ClassifyDataParameter criteria, Path field, String dialect,
			Object value) {
		return null;
	}

	default Expression<?> getOrder(SearchParameter searchParameter, String dialect, Path fieldPath,
			CriteriaBuilder builder) {
		return null;
	}

}
