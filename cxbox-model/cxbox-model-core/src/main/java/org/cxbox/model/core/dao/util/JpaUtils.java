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

package org.cxbox.model.core.dao.util;

import org.cxbox.model.core.entity.BaseEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.experimental.UtilityClass;
import org.hibernate.Hibernate;

@UtilityClass
public final class JpaUtils {

	public static <T> T getSingleResult(final TypedQuery<T> typedQuery) {
		final List<T> resultList = typedQuery.getResultList();
		if (resultList.isEmpty()) {
			throw new NoResultException();
		}
		if (resultList.size() == 1) {
			return resultList.get(0);
		}
		throw new NonUniqueResultException("result returns more than one elements");
	}

	public static <T> T getSingleResultOrNull(final TypedQuery<T> typedQuery) {
		final List<T> resultList = typedQuery.getResultList();
		if (resultList.isEmpty()) {
			return null;
		}
		if (resultList.size() == 1) {
			return resultList.get(0);
		}
		throw new NonUniqueResultException("result returns more than one elements");
	}

	public static <T> T getFirstResultOrNull(final TypedQuery<T> typedQuery) {
		final List<T> resultList = typedQuery.getResultList();
		if (resultList.isEmpty()) {
			return null;
		}
		return resultList.get(0);
	}

	public static void executeNativeQuery(final EntityManager entityManager, final String sql, final Object... params) {
		final Query query = entityManager.createNativeQuery(sql);
		bindParameters(query, params);
		query.executeUpdate();
	}

	public static <T> List<T> selectNativeQuery(final EntityManager entityManager, final String sql, final Object... params) {
		final Query query = entityManager.createNativeQuery(sql);
		bindParameters(query, params);
		return query.getResultList();
	}

	public static <T> List<T> selectNativeQuery(final EntityManager entityManager, Class<T> resultClass, final String sql,
			final Object... params) {
		final Query query = entityManager.createNativeQuery(sql, resultClass);
		bindParameters(query, params);
		return query.getResultList();
	}

	/**
	 * @param entityManager entityManager
	 * @param procedureName name of the stored procedure in the database
	 * @param input input parameters with their values
	 * @param output output parameter names
	 * @return output parameters with their values
	 */
	public static Map<String, Object> executeStoredProcedure(final EntityManager entityManager, final String procedureName,
			final Map<Integer, Object> input, final List<String> output) {
		final StoredProcedureQuery query = entityManager.createStoredProcedureQuery(procedureName);
		input.forEach((key, value) -> {
			query.registerStoredProcedureParameter(key, value.getClass(), ParameterMode.IN);
			query.setParameter(key, value);
		});
		output.forEach(param -> query.registerStoredProcedureParameter(param, String.class, ParameterMode.OUT));
		query.execute();
		Map<String, Object> result = new HashMap<>();
		output.forEach(param -> result.put(param, query.getOutputParameterValue(param)));
		return result;
	}

	private static void bindParameters(Query query, Object[] params) {
		for (int i = 0; i < params.length; i++) {
			query.setParameter(i + 1, params[i]);
		}
	}

	public static <X, Z, Y> Join<X, Z> addJoin(From<Y, X> from, SingularAttribute<X, Z> attribute, JoinType joinType) {
		Set<Join<X, ?>> joins = from.getJoins();
		Join existedJoin = joins.stream().filter(join -> join.getAttribute().equals(attribute)).findFirst().orElse(null);
		if (existedJoin == null) {
			return from.join(attribute, joinType);
		} else {
			return existedJoin;
		}
	}

	public static Class unproxiedClass(BaseEntity proxy) {
		return Hibernate.getClass(proxy);
	}

}
