/*-
 * #%L
 * IO Cxbox - Workflow API
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.source.services.data.impl;

import static org.cxbox.core.controller.param.SearchOperation.CONTAINS;
import static org.cxbox.core.controller.param.SearchOperation.CONTAINS_ONE_OF;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS_ONE_OF;
import static org.cxbox.core.controller.param.SearchOperation.SPECIFIED;

import org.cxbox.api.data.ResultPage;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.controller.param.FilterParameters;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.crudma.impl.AbstractResponseService;
import org.cxbox.source.dto.AdminBcDto;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.ui.entity.View;
import org.cxbox.model.ui.entity.ViewWidgets;
import org.cxbox.model.ui.entity.ViewWidgets_;
import org.cxbox.model.ui.entity.View_;
import org.cxbox.model.ui.entity.Widget;
import org.cxbox.model.ui.entity.Widget_;
import org.cxbox.source.services.data.TaskChildBcService;
import org.cxbox.source.services.data.WorkflowableTaskService;
import org.cxbox.source.services.meta.TaskChildBcFieldMetaBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class TaskChildBcServiceImpl extends AbstractResponseService<AdminBcDto, BaseEntity> implements
		TaskChildBcService {

	@Autowired
	private List<EntityManager> entityManagers;

	@Autowired
	private BcRegistry bcRegistry;

	public TaskChildBcServiceImpl() {
		super(AdminBcDto.class, BaseEntity.class, null, TaskChildBcFieldMetaBuilder.class);
	}

	@Override
	public ResultPage<AdminBcDto> getList(BusinessComponent bc) {
		QueryParameters params = bc.getParameters();
		List<BcDescription> saValues = getAllBusCompBelowTask();
		Map<String, String> allViewsByBcNames = getAllViewsByBcNames(
				saValues.stream().map(BcDescription::getName).collect(Collectors.toList())
		);
		List<AdminBcDto> resultList = saValues.stream().map(bcDescription -> {
			AdminBcDto adminBcDto = new AdminBcDto(bcDescription);
			adminBcDto.setAffectedWidgets(allViewsByBcNames.get(bcDescription.getName()));
			return adminBcDto;
		}).filter(dto -> filterByQueryParams(dto, params.getFilter()))
				.collect(Collectors.toList());
		resultList = resultList.stream().skip(params.getPageNumber() * params.getPageSize())
				.limit(params.getPageSize() + 1).collect(Collectors.toList());
		return dtoListToResultPage(resultList, params.getPageSize());
	}

	private Map<String, String> getAllViewsByBcNames(List<String> bcNames) {
		EntityManager entityManager = getSupportedEntityManager(ViewWidgets.class.getName());
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<ViewWidgets> viewWidgetsRoot = cq.from(ViewWidgets.class);
		Root<View> viewRoot = cq.from(View.class);
		Join<ViewWidgets, Widget> widgetJoin = viewWidgetsRoot.join(ViewWidgets_.widget);
		cq.multiselect(widgetJoin.get(Widget_.bc), widgetJoin.get(Widget_.title), viewRoot.get(View_.title));
		cq.where(cb.and(
				cb.or(bcNames.stream()
						.map(bcName -> cb.equal(widgetJoin.get(Widget_.bc), bcName))
						.toArray(Predicate[]::new)
				),
				cb.equal(viewWidgetsRoot.get(ViewWidgets_.viewName), viewRoot.get(View_.name))
		));
		List<Tuple> resultList = entityManager.createQuery(cq).getResultList();
		Map<String, String> result = new HashMap<>();
		for (Tuple tuple : resultList) {
			LinkedList<Object> objects = new LinkedList<>(Arrays.asList(tuple.toArray()));
			String bc = (String) objects.get(0);
			String name = objects.get(1) + " -> " + objects.get(2);
			if (result.containsKey(bc)) {
				result.put(bc, result.get(bc) + "\n" + name);
			} else {
				result.put(bc, name);
			}
		}
		return result;
	}

	private List<BcDescription> getAllBusCompBelowTask() {
		List<BcDescription> saValues = bcRegistry.select(bc -> bc.getParentName() != null)
				.filter(bc -> isWorkflowBc(bcRegistry.getBcDescription(bc.getParentName())))
				.collect(Collectors.toList());
		List<BcDescription> addonialSaValues = new ArrayList<>(saValues);
		do {
			List<BcDescription> finalAddonialSaValues = addonialSaValues;
			addonialSaValues = bcRegistry.select(sa -> finalAddonialSaValues.contains(
					Optional.of(sa).map(BcDescription::getParentName)
							.map(bcRegistry::getBcDescription).orElse(null))
			).collect(Collectors.toList());
			saValues.addAll(addonialSaValues);
		} while (!addonialSaValues.isEmpty());
		return saValues;
	}

	private boolean filterByQueryParams(AdminBcDto dto, FilterParameters searchParameters) {
		for (FilterParameter parameter : searchParameters) {
			if ("name".equals(parameter.getName())) {
				if (parameter.getOperation() == EQUALS) {
					return Objects.equals(parameter.getStringValue(), dto.getName());
				}
				if (parameter.getOperation() == SPECIFIED) {
					if (parameter.getBooleanValue()) {
						return dto.getName() != null;
					} else {
						return dto.getName() == null;
					}
				} else if (parameter.getOperation() == CONTAINS) {
					if (parameter.getStringValue() == null) {
						return true;
					} else if (dto.getName() == null) {
						return false;
					} else {
						return dto.getName().contains(parameter.getStringValue());
					}
				} else if (parameter.getOperation() == CONTAINS_ONE_OF) {
					return parameter.getStringValuesAsList().stream().anyMatch(param -> {
								if (param == null) {
									return true;
								} else if (dto.getName() == null) {
									return false;
								} else {
									return dto.getName().contains(param);
								}
							}
					);
				} else if (parameter.getOperation() == EQUALS_ONE_OF) {
					return parameter.getStringValuesAsList().stream().anyMatch(param ->
							Objects.equals(param, dto.getName())
					);
				}
			}
			if ("affectedWidgets".equals(parameter.getName())) {
				if (parameter.getOperation() == EQUALS) {
					return Objects.equals(parameter.getStringValue(), dto.getAffectedWidgets());
				}
				if (parameter.getOperation() == SPECIFIED) {
					if (parameter.getBooleanValue()) {
						return dto.getAffectedWidgets() != null;
					} else {
						return dto.getAffectedWidgets() == null;
					}
				} else if (parameter.getOperation() == CONTAINS) {
					if (parameter.getStringValue() == null) {
						return true;
					} else if (dto.getAffectedWidgets() == null) {
						return false;
					} else {
						return dto.getAffectedWidgets().contains(parameter.getStringValue());
					}
				}
			}
		}
		return true;
	}

	@Override
	public long count(BusinessComponent bc) {
		return bcRegistry.select(this::isWorkflowBc).count();
	}

	private boolean isWorkflowBc(BcDescription bcDescription) {
		return bcDescription instanceof InnerBcDescription
				&& WorkflowableTaskService.class.isAssignableFrom(
				((InnerBcDescription) bcDescription).getServiceClass()
		);
	}

	protected EntityManager getSupportedEntityManager(String entityClazz) {
		List<EntityManager> supportedEntityManagers = entityManagers.stream().filter(
				entityManager -> entityManager.getMetamodel().getEntities().stream().anyMatch(
						//todo: delete check simpleName in next major release
						entityType -> com.google.common.base.Objects.equal(entityType.getBindableJavaType().getSimpleName(), entityClazz)
								|| com.google.common.base.Objects.equal(entityType.getBindableJavaType().getName(), entityClazz)
				)
		).collect(Collectors.toList());
		if (supportedEntityManagers.size() == 1) {
			return supportedEntityManagers.get(0);
		} else {
			throw new IllegalArgumentException("Can't find unique EntityManager for entity: " + entityClazz);
		}
	}

}
