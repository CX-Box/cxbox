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

package org.cxbox.core.dao.impl;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.ResultPage;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.core.controller.param.FilterParameters;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.controller.param.SortParameters;
import org.cxbox.core.dao.BaseDAO;
import org.cxbox.core.dao.IPdqExtractor;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import org.cxbox.model.core.dao.impl.DialectName;
import org.cxbox.model.core.dao.impl.JpaDaoImpl;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@Transactional
public class BaseDAOImpl extends JpaDaoImpl implements BaseDAO {

	private final Optional<IPdqExtractor> pdqExtractor;

	private final List<ClassifyDataProvider> providers;

	public BaseDAOImpl(
			Set<EntityManager> entityManagers,
			TransactionService txService,
			Optional<IPdqExtractor> pdqExtractor,
			List<ClassifyDataProvider> providers
	) {
		super(entityManagers, txService);
		this.pdqExtractor = pdqExtractor;
		this.providers = providers;
	}

	private Specification getPdqSearchSpec(final QueryParameters queryParameters) {
		return pdqExtractor.map(pdqExtractor -> pdqExtractor.extractPdq(queryParameters.getPdqName())).orElse(null);
	}

	@Override
	public <T, X> Long getCount(Class<T> clazz, Class dtoClazz, SingularAttribute<T, X> name, X value,
			QueryParameters queryParameters) {
		return getCount(clazz, dtoClazz, (root, cq, cb) -> cb.equal(root.get(name), value), queryParameters);
	}

	@Override
	public <T> ResultPage<T> getList(Class<T> entityClazz, Class dtoClazz,
			Specification<T> defaultSearchSpec, QueryParameters parameters) {
		return getList(entityClazz, dtoClazz, defaultSearchSpec, parameters, null);
	}

	@Override
	public <T> ResultPage<T> getList(CriteriaQuery<T> cq, Root<T> root, Class dtoClazz,
			Predicate defaultSearchSpec, QueryParameters parameters) {
		return getList(cq, root, dtoClazz, defaultSearchSpec, parameters, null);
	}

	public <T> Predicate getPredicateFromSearchParams(Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder cb,
			Class dtoClazz,
			FilterParameters searchParams,
			DialectName dialect
	) {
		return MetadataUtils.getPredicateFromSearchParams(root, cq, cb, dtoClazz, searchParams, providers, dialect);
	}

	@Override
	public <T> Long getCount(
			CriteriaQuery<Long> cq,
			Root<T> root,
			Class dtoClazz,
			Predicate defaultSearchSpec,
			QueryParameters queryParameters
	) {
		EntityManager entityManager = getSupportedEntityManager(root.getModel().getBindableJavaType().getName());
		DialectName dialect = getDialect(entityManager);
		queryParameters = emptyIfNull(queryParameters);
		FilterParameters searchParams = queryParameters.getFilter();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		if (defaultSearchSpec == null) {
			defaultSearchSpec = cb.and();
		}
		Specification pdqSearchSpec = getPdqSearchSpec(queryParameters);
		if (pdqSearchSpec == null) {
			pdqSearchSpec = (root1, cq1, cb1) -> cb1.and();
		}
		cq.select(cb.count(root));
		Predicate searchParamsPredicate = getPredicateFromSearchParams(root, cq, cb, dtoClazz, searchParams, dialect);
		cq.where(cb.and(defaultSearchSpec, searchParamsPredicate, pdqSearchSpec.toPredicate(root, cq, cb)));
		return entityManager.createQuery(cq).getSingleResult();
	}

	@Override
	public <T> Long getCount(
			Class<T> entityClass,
			Class<?> dtoClazz,
			Specification<T> searchSpec,
			QueryParameters queryParameters
	) {
		EntityManager entityManager = getSupportedEntityManager(entityClass.getName());
		DialectName dialect = getDialect(entityManager);
		queryParameters = emptyIfNull(queryParameters);
		FilterParameters parameters = queryParameters.getFilter();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<T> root = cq.from(entityClass);
		cq.select(cb.count(root));
		List<Predicate> predicates = new ArrayList<>();
		if (searchSpec != null) {
			predicates.add(searchSpec.toPredicate(root, cq, cb));
		}
		Specification pdqSearchSpec = getPdqSearchSpec(queryParameters);
		if (pdqSearchSpec != null) {
			predicates.add(pdqSearchSpec.toPredicate(root, cq, cb));
		}
		predicates.add(getPredicateFromSearchParams(root, cq, cb, dtoClazz, parameters, dialect));
		cq.where(cb.and(predicates.toArray(new Predicate[0])));
		return entityManager.createQuery(cq).getSingleResult();
	}

	@Override
	public <T> ResultPage<T> getList(
			CriteriaQuery<T> cq,
			Root<T> root,
			Class dtoClazz,
			Predicate defaultSearchSpec,
			QueryParameters parameters,
			EntityGraph<? super T> fetchGraph
	) {
		EntityManager entityManager = getSupportedEntityManager(root.getModel().getBindableJavaType().getName());
		DialectName dialect = getDialect(entityManager);
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		SortParameters sort = parameters.getSort();
		FilterParameters filter = parameters.getFilter();
		if (defaultSearchSpec == null) {
			defaultSearchSpec = cb.and();
		}
		Specification pdqSearchSpec = getPdqSearchSpec(parameters);
		if (pdqSearchSpec == null) {
			pdqSearchSpec = (root1, cq1, cb1) -> cb1.and();
		}

		Predicate searchParamsPredicate = getPredicateFromSearchParams(root, cq, cb, dtoClazz, filter, dialect);

		if (cq.getRestriction() != null) {
			cq.where(cb.and(
					cq.getRestriction(),
					defaultSearchSpec,
					searchParamsPredicate,
					pdqSearchSpec.toPredicate(root, cq, cb)
			));
		} else {
			cq.where(cb.and(
					defaultSearchSpec,
					searchParamsPredicate,
					pdqSearchSpec.toPredicate(root, cq, cb)
			));
		}
		MetadataUtils.addSorting(dtoClazz, root, cq, cb, sort, dialect);
		applyGraph(root, fetchGraph);

		Query<T> query = entityManager.unwrap(Session.class).createQuery(cq);
		applyPaging(query, parameters.getPage());

		return ResultPage.of(query.getResultList(), parameters.getPage());
	}

	@Override
	public <T> ResultPage<T> getList(
			Class<T> entityClazz,
			Class dtoClazz,
			Specification<T> defaultSearchSpec,
			QueryParameters parameters,
			EntityGraph<? super T> fetchGraph
	) {
		EntityManager entityManager = getSupportedEntityManager(entityClazz.getName());
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClazz);
		Root<T> root = cq.from(entityClazz);
		Predicate predicate = defaultSearchSpec.toPredicate(root, cq, cb);
		return getList(cq, root, dtoClazz, predicate, parameters, fetchGraph);
	}


	private QueryParameters emptyIfNull(QueryParameters queryParameters) {
		if (queryParameters == null) {
			return QueryParameters.emptyQueryParameters();
		}
		return queryParameters;
	}

}
