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

import static org.cxbox.api.data.dictionary.DictionaryCache.dictionary;
import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;
import static org.cxbox.core.controller.param.SortType.ASC;
import static org.cxbox.core.controller.param.SortType.DESC;

import autovalue.shaded.org.checkerframework.checker.nullness.qual.Nullable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.Bindable;
import jakarta.persistence.metamodel.Bindable.BindableType;
import jakarta.persistence.metamodel.ManagedType;
import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.cxbox.api.data.dictionary.IDictionaryType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.util.CxReflectionUtils;
import org.cxbox.core.controller.param.FilterParameters;
import org.cxbox.core.controller.param.SortParameter;
import org.cxbox.core.controller.param.SortParameters;
import org.cxbox.core.dao.ClassifyDataParameter;
import org.cxbox.core.dto.LovUtils;
import org.cxbox.core.util.filter.MultisourceSearchParameter;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.core.util.filter.provider.ClassifyDataProvider;
import org.cxbox.core.util.filter.provider.impl.BooleanValueProvider;
import org.cxbox.core.util.filter.provider.impl.MultisourceValueProvider;
import org.cxbox.core.util.filter.provider.impl.TimeValueProvider;
import org.cxbox.model.core.entity.BaseEntity;
import org.springframework.data.jpa.domain.Specification;


@Slf4j
@UtilityClass
public class MetadataUtils {

	public List<ClassifyDataParameter> mapSearchParamsToPOJO(Class dtoClazz, FilterParameters filterParameters,
			String dialect,
			List<ClassifyDataProvider> providers) {

		List<ClassifyDataParameter> result = new ArrayList<>();

		filterParameters.forEach(filterParam -> {
					try {
						Field dtoField = Optional.ofNullable(CxReflectionUtils.findField(dtoClazz, filterParam.getName()))
								.orElseThrow(
										() -> new IllegalArgumentException(
												errorMessage(
														"error.class_field_not_found",
														filterParam.getName(),
														dtoClazz.getName()
												)
										)
								);
						MultisourceSearchParameter multisourceParameter = dtoField
								.getDeclaredAnnotation(MultisourceSearchParameter.class);
						if (multisourceParameter != null) {
							providers.stream().filter(p -> p.getClass().equals(multisourceParameter.provider()))
									.findFirst()
									.ifPresent(
											dataProvider -> result.addAll(dataProvider.getClassifyDataParameters(
													dtoField,
													filterParam,
													null,
													providers
											))
									);
						} else {
							SearchParameter searchParam = Optional.ofNullable(dtoField.getDeclaredAnnotation(SearchParameter.class))
									.orElseThrow(
											() -> new IllegalArgumentException(
													errorMessage(
															"error.missing_search_parameter_annotation",
															filterParam.getName()
													)
											)
									);
							providers.stream().filter(p -> p.getClass().equals(searchParam.provider()))
									.findFirst()
									.ifPresent(
											dataProvider -> result.addAll(dataProvider.getClassifyDataParameters(
													dtoField,
													filterParam,
													searchParam,
													providers
											))
									);
						}
					} catch (Exception e) {
						log.warn(errorMessage("error.failed_to_parse_filter_param", filterParam), e);
					}

				}
		);
		return result;
	}

	public static boolean mayBeNull(Root<?> root, Path path) {
		Bindable model = path.getModel();
		BindableType type = model.getBindableType();
		if (type != BindableType.SINGULAR_ATTRIBUTE) {
			return true;
		}
		// джойн
		if (path.getParentPath() != root) {
			return true;
		}
		return !model.getBindableJavaType().isPrimitive();
	}

	public static Comparable requireComparable(Object value) {
		if (value instanceof Comparable) {
			return (Comparable) value;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public static String requireString(Object value) {
		if (value instanceof String) {
			return (String) value;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public static JoinType getJoinType(From from, String attrName) {
		JoinType joinType = JoinType.INNER;
		Bindable model = from.getModel();
		if (model.getBindableType() == BindableType.ENTITY_TYPE) {
			ManagedType managedType = (ManagedType) model;
			if (managedType.getAttribute(attrName).isAssociation()) {
				joinType = JoinType.LEFT;
			}
		}
		return joinType;
	}

	public static JoinType getJoinType(FetchParent fetch, String attrName) {
		// todo: похоже так всегда и бывает
		if (fetch instanceof From) {
			return getJoinType((From) fetch, attrName);
		}
		return JoinType.LEFT;
	}

	@SuppressWarnings("unchecked")
	public static Join joinEntity(From from, String attrName) {
		Set<Join> joins = from.getJoins();
		for (Join join : joins) {
			if (join.getAttribute().getName().equals(attrName)) {
				return join;
			}
		}
		return from.join(attrName, getJoinType(from, attrName));
	}

	public static Path getFieldPath(String fieldName, Root<?> root) {
		Path result;
		if (fieldName.contains(".")) {
			String[] fieldArr = fieldName.split("\\.");
			From partialFrom = root;
			for (int i = 0; i < fieldArr.length - 1; i++) {
				partialFrom = joinEntity(partialFrom, fieldArr[i]);
			}
			return partialFrom.get(fieldArr[fieldArr.length - 1]);
		}
		if (isElementCollectionField(root, fieldName)) {
			return root.join(fieldName);
		}
		return root.get(fieldName);
	}

	public static Predicate createPredicate(Root<?> root, ClassifyDataParameter criteria, CriteriaBuilder cb,
			String dialect) {
		try {
			Object value = criteria.getValue();

			Path field = getFieldPath(criteria.getField(), root);

			Expression filterPredicateForTime = TimeValueProvider.getFilterPredicate(cb, criteria, field, dialect, value);
			switch (criteria.getOperator()) {
				case EQUALS:
					if (filterPredicateForTime != null) {
						return cb.equal(filterPredicateForTime, requireComparable(value));
					} else if (value instanceof String) {
						return cb.equal(cb.upper(field), requireString(value).toUpperCase());
					} else {
						return cb.equal(field, value);
					}
				case CONTAINS:
					return cb.like(cb.upper(field), "%" + requireString(value).toUpperCase() + "%");
				case GREATER_THAN:
					if (filterPredicateForTime != null) {
						return cb.greaterThan(filterPredicateForTime, requireComparable(value));
					}
					return cb.greaterThan(
							filterPredicateForTime != null ? filterPredicateForTime : field,
							requireComparable(value)
					);
				case LESS_THAN:
					if (filterPredicateForTime != null) {
						return cb.lessThan(filterPredicateForTime, requireComparable(value));
					}
					return cb.lessThan(field, requireComparable(value));
				case GREATER_OR_EQUAL_THAN:
					if (filterPredicateForTime != null) {
						return cb.greaterThanOrEqualTo(filterPredicateForTime, requireComparable(value));
					}
					return cb.greaterThanOrEqualTo(field, requireComparable(value));
				case LESS_OR_EQUAL_THAN:
					if (filterPredicateForTime != null) {
						return cb.lessThanOrEqualTo(filterPredicateForTime, requireComparable(value));
					}
					return cb.greaterThanOrEqualTo(field, requireComparable(value));
				case SPECIFIED:
					boolean isSpecified = BooleanUtils.isTrue((Boolean) value);
					if (BooleanValueProvider.class.equals(criteria.getProvider())) {
						return isSpecified ?
								cb.equal(field, true) :
								mayBeNull(root, field) ?
										cb.or(cb.isNull(field), cb.equal(field, false)) :
										cb.equal(field, false);
					} else {
						return isSpecified ?
								cb.isNotNull(field) :
								cb.isNull(field);
					}
				case EQUALS_ONE_OF:
					if (((List<Object>) value).stream().allMatch(((s) -> s instanceof String))) {
						return cb.or(((List<Object>) value).stream()
								.map(object -> cb.equal(cb.upper(field), requireString(object).toUpperCase()))
								.toArray(Predicate[]::new));
					} else if (filterPredicateForTime != null) {
						return cb
								.or(((List<Object>) value).stream().map(object -> cb.equal(
										filterPredicateForTime,
										requireComparable(value)
								)).toArray(Predicate[]::new));
					} else {
						return cb
								.or(((List<Object>) value).stream().map(object -> cb.equal(field, object)).toArray(Predicate[]::new));
					}
				case CONTAINS_ONE_OF:
					return cb.or(((List<Object>) value)
							.stream()
							.map(object -> cb
									.like(cb.upper(field), "%".concat(requireString(object).toUpperCase()).concat("%")))
							.toArray(Predicate[]::new));
				default:
					throw new IllegalArgumentException();
			}
		} catch (Exception e) {
			log.warn(
					"error when try to parse search expr: "
							+ criteria.getField() + "." + criteria.getOperator() + "=" + criteria.getValue(), e
			);
			return null;
		}
	}

	public static <T> void addSorting(final Class dtoClazz, final Root<?> root, final CriteriaQuery<T> query,
			CriteriaBuilder builder, final SortParameters sort, String dialect) {
		List<Order> orderList = new ArrayList<>();
		if (!query.getOrderList().isEmpty()) {
			orderList.addAll(query.getOrderList());
		}
		List<SortParameter> sortedParams = sort.getParameters().stream()
				.sorted(Comparator.comparingInt(SortParameter::getPriority)).collect(Collectors.toList());
		for (SortParameter p : sortedParams) {
			try {
				String field = getSortField(dtoClazz, p);
				Path fieldPath = getFieldPath(field, root);
				IDictionaryType lovType = getLovType(dtoClazz, p);
				Expression<?> order;
				SearchParameter searchParameter = getSearchParameter(dtoClazz, p);
				if (lovType != null) {
					Collection<SimpleDictionary> dictDTOS = dictionary().getAll(lovType);
					CriteriaBuilder.Case<String> selectCase = builder.selectCase();
					dictDTOS.forEach(dictDTO -> selectCase.when(
							builder.equal(fieldPath, new LOV(dictDTO.getKey())), dictDTO.getValue()
					));
					order = selectCase.otherwise("");
				} else {
					order = TimeValueProvider.getOrder(searchParameter, dialect, fieldPath, builder);
					if (order == null) {
						order = fieldPath;
					}
				}

				if (ASC.equals(p.getType())) {
					orderList.add(builder.asc(order));
				} else if (DESC.equals(p.getType())) {
					orderList.add(builder.desc(order));
				}
			} catch (Exception exception) {
				log.warn(
						"Couldn't parse sorting parameter {}",
						Optional.ofNullable(dtoClazz).map(c -> "for class " + c.getName()).orElse(""),
						exception
				);
			}
		}
		if (BaseEntity.class.isAssignableFrom(root.getJavaType())) {
			orderList.add(builder.desc(root.get("id")));
		}
		query.orderBy(orderList);
	}

	private static String getSortField(Class dtoClazz, SortParameter parameter) {
		String field;
		if (dtoClazz == null) {
			field = parameter.getName();
		} else {
			SearchParameter fieldParameter = getSearchParameter(dtoClazz, parameter);
			if (fieldParameter != null && !"".equals(fieldParameter.name())) {
				field = fieldParameter.name();
			} else {
				field = parameter.getName();
			}
		}
		return field;
	}

	private static SearchParameter getSearchParameter(@NonNull Class dtoClazz, SortParameter parameter) {
		Field dtoField = CxReflectionUtils.findField(dtoClazz, parameter.getName());
		if (dtoField == null) {
			throw new IllegalArgumentException(
					"Couldn't find field " + parameter.getName() + " in class " + dtoClazz.getName());
		}
		return dtoField.getDeclaredAnnotation(org.cxbox.core.util.filter.SearchParameter.class);

	}

	private static IDictionaryType getLovType(Class dtoClazz, SortParameter parameter) {
		if (dtoClazz != null) {
			Field dtoField = CxReflectionUtils.findField(dtoClazz, parameter.getName());
			if (dtoField == null) {
				throw new IllegalArgumentException(
						"Couldn't find field " + parameter.getName() + " in class " + dtoClazz.getName());
			}
			return LovUtils.getType(dtoField);
		}
		return null;
	}

	public static <T> Predicate getPredicateFromSearchParams(Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder cb,
			Class dtoClazz,
			FilterParameters searchParams,
			String dialect, List<ClassifyDataProvider> providers) {

		if (searchParams == null) {
			return cb.and();
		}
		List<ClassifyDataParameter> criteriaStrings = mapSearchParamsToPOJO(
				dtoClazz,
				searchParams,
				dialect,
				providers
		);
		boolean joinRequired = criteriaStrings.stream()
				.anyMatch(param -> param.getField().contains("."));

		Predicate filterPredicate;
		if (joinRequired) {
			Subquery<Long> filterSubquery = cq.subquery(Long.class);
			Class<T> rootClass = root.getModel().getJavaType();
			Root<T> subRoot = filterSubquery.from(rootClass);
			Predicate searchParamsRestriction = getAllSpecifications(cb, subRoot, criteriaStrings, dialect);
			filterSubquery.select(subRoot.get("id"))
					.where(searchParamsRestriction);
			filterPredicate = cb.in(root.get("id")).value(filterSubquery);
		} else {
			filterPredicate = getAllSpecifications(cb, root, criteriaStrings, dialect);
		}
		return filterPredicate;
	}

	public static Predicate getAllSpecifications(CriteriaBuilder cb, Root<?> root,
			List<ClassifyDataParameter> criteriaStrings, String dialect) {
		return cb.and(criteriaStrings.stream()
				.map(criteria -> getSingleSpecification(cb, root, criteria, dialect))
				.filter(Objects::nonNull).toArray(Predicate[]::new));
	}

	private static Predicate getSingleSpecification(CriteriaBuilder cb, Root<?> root, ClassifyDataParameter criteria,
			String dialect) {
		if (MultisourceValueProvider.class.equals(criteria.getProvider())) {
			List criteriaValue = (List) criteria.getValue();
			List<Predicate> predicates = new ArrayList<>();
			for (Object innerList : criteriaValue) {
				predicates.add(getAllSpecifications(cb, root, (List) innerList, dialect));
			}
			return cb.or(predicates.stream().filter(Objects::nonNull).toArray(Predicate[]::new));
		} else {
			return createPredicate(root, criteria, cb, dialect);
		}
	}

	private boolean isElementCollectionField(Root<?> root, String fieldName) {
		Class<?> rootClass = root.getModel().getJavaType();
		Field field = CxReflectionUtils.findField(
				rootClass,
				fieldName
		);
		return Optional.ofNullable(field)
				.map(fld -> fld.isAnnotationPresent(ElementCollection.class))
				.orElseThrow(() -> new IllegalArgumentException(
								String.format(
										"Couldn't find field %s in entity %s",
										fieldName,
										rootClass.getName()
								)
						)
				);
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
	 * so always add functional index CREATE INDEX idx_apple_created_date_time ON apple ((created_date::time));
	 * <br>
	 * <h6>dialect = Oracle</h6>
	 * Column in DB: TIMESTAMP
	 * <br>
	 * Actual SQL:
	 * {@code Query:["select a1_0.id,a1_0.commend,a1_0.created_date from apple a1_0 order by to_char(a1_0.created_date,'HH24:MI:SS') asc"]}
	 * so always add functional index ???????????;
	 * <br>
	 */
	static Specification sortByTimePart(@Nullable String dialect, @NonNull Path field) {
		if (dialect.contains("Oracle")) {
			return (root, query, cb) -> {
				Expression<String> timeExpr = cb.function("TO_CHAR", String.class, field, cb.literal("HH24:MI:SS"));
				query.orderBy(cb.asc(timeExpr));
				return null;
			};
		}
		return (root, query, builder) -> {
			query.orderBy(builder.asc(field.as(LocalTime.class)));
			return null;
		};

	}

}

