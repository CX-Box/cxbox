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

package org.cxbox.core.dao;

import org.cxbox.api.data.ResultPage;
import org.cxbox.core.controller.param.FilterParameters;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.model.core.dao.JpaDao;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;


public interface BaseDAO extends JpaDao {

	<T, X> Long getCount(Class<T> clazz, Class dtoClazz, SingularAttribute<T, X> name, X value,
			QueryParameters queryParameters);

	<T> Long getCount(CriteriaQuery<Long> cq, Root<T> root, Class dtoClazz, Predicate defaultSearchSpec,
			QueryParameters parameters);

	<T> Long getCount(Class<T> entityClass, Class<?> dtoClazz, Specification<T> searchSpec, QueryParameters parameters);

	<T> ResultPage<T> getList(CriteriaQuery<T> cq, Root<T> root, Class dtoClazz, Predicate defaultSearchSpec,
			QueryParameters parameters);

	<T> ResultPage<T> getList(CriteriaQuery<T> cq, Root<T> root, Class dtoClazz, Predicate defaultSearchSpec,
			QueryParameters parameters, EntityGraph<? super T> fetchGraph);

	<T> ResultPage<T> getList(Class<T> entityClazz, Class dtoClazz, Specification<T> defaultSearchSpec,
			QueryParameters parameters);

	<T> ResultPage<T> getList(Class<T> entityClazz, Class dtoClazz, Specification<T> defaultSearchSpec,
			QueryParameters parameters, EntityGraph<? super T> fetchGraph);

	<T> Predicate getPredicateFromSearchParams(Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder cb,
			Class dtoClazz,
			FilterParameters searchParams,
			String dialect);


}
