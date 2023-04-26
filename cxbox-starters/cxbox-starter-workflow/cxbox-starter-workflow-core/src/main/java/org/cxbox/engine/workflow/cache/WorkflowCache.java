/*-
 * #%L
 * IO Cxbox - Workflow Impl
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

package org.cxbox.engine.workflow.cache;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.workflow.entity.WorkflowCondition;
import org.cxbox.model.workflow.entity.WorkflowCondition_;
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowStepConditionGroup;
import org.cxbox.model.workflow.entity.WorkflowStepField;
import org.cxbox.model.workflow.entity.WorkflowStepField_;
import org.cxbox.model.workflow.entity.WorkflowStep_;
import org.cxbox.model.workflow.entity.WorkflowTaskChildBcAvailability;
import org.cxbox.model.workflow.entity.WorkflowTaskChildBcAvailability_;
import org.cxbox.model.workflow.entity.WorkflowTransition;
import org.cxbox.model.workflow.entity.WorkflowTransitionConditionGroup;
import org.cxbox.model.workflow.entity.WorkflowTransitionConditionGroup_;
import org.cxbox.model.workflow.entity.WorkflowTransition_;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class WorkflowCache {

	private final JpaDao jpaDao;

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = CacheConfig.WORKFLOW_CACHE, key = "{#root.methodName, #sourceStep.id}")
	public List<WorkflowTransition> getTransitions(final WorkflowStep sourceStep) {
		return jpaDao.getList(WorkflowTransition.class, (root, query, cb) -> {
			root.fetch(WorkflowTransition_.destinationStep);
			return cb.and(
					cb.equal(root.get(WorkflowTransition_.sourceStep), sourceStep),
					cb.equal(
							root.get(WorkflowTransition_.destinationStep).get(WorkflowStep_.workflowVersion),
							sourceStep.getWorkflowVersion()
					)
			);
		}).stream().distinct().map(jpaDao::evict).collect(Collectors.toList());
	}

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = CacheConfig.WORKFLOW_CACHE, key = "{#root.methodName, #transition.id, #condGroupCd}")
	public List<WorkflowTransitionConditionGroup> getTransitionConditionGroups(final WorkflowTransition transition,
			final LOV condGroupCd) {
		return jpaDao.getList(WorkflowTransitionConditionGroup.class, (root, query, cb) -> cb.and(
				cb.equal(root.get(WorkflowTransitionConditionGroup_.transition), transition),
				cb.equal(root.get(WorkflowTransitionConditionGroup_.condGroupCd), condGroupCd)
		)).stream().distinct().map(jpaDao::evict)
				.sorted(comparing(WorkflowTransitionConditionGroup::getSeq, nullsLast(naturalOrder())))
				.collect(Collectors.toList());
	}

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = CacheConfig.WORKFLOW_CACHE, key = "{#root.methodName, #workflowStep.id}")
	public List<WorkflowStepField> getStepFields(final WorkflowStep workflowStep) {
		return jpaDao.getList(
				WorkflowStepField.class,
				(root, query, cb) -> cb.equal(root.get(WorkflowStepField_.step), workflowStep)
		).stream().map(jpaDao::evict).collect(Collectors.toList());
	}

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = CacheConfig.WORKFLOW_CACHE, key = "{#root.methodName, #workflowStep.id}")
	public WorkflowStep getFirstWorkflowStep(final WorkflowStep workflowStep) {
		return workflowStep.getWorkflowVersion().getFirstStep();
	}

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = CacheConfig.WORKFLOW_CACHE, key = "{#root.methodName, #workflowStep.id}")
	public List<WorkflowTaskChildBcAvailability> getWorkflowTaskChildBcAvailabilities(final WorkflowStep workflowStep) {
		return jpaDao.getList(WorkflowTaskChildBcAvailability.class, (root, query, cb) -> cb.equal(root
				.get(WorkflowTaskChildBcAvailability_.workflowStep), workflowStep));
	}

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = CacheConfig.WORKFLOW_CACHE, key = "{#root.methodName, #conditionGroup.id}")
	public <C extends WorkflowCondition> List<C> getTransitionConditions(
			final Class<C> conditionClass,
			final WorkflowTransitionConditionGroup conditionGroup) {
		final List<C> result = jpaDao.getList(conditionClass, (root, query, cb) -> {
			jpaDao.applyGraph(root, jpaDao.getEntityGraph(conditionClass, "cache"));
			return cb.equal(
					root.get(WorkflowCondition_.transitionConditionGroup), conditionGroup
			);
		});
		result.sort(comparing(WorkflowCondition::getSeq, nullsFirst(naturalOrder())));
		return result;
	}

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = CacheConfig.WORKFLOW_CACHE, key = "{#root.methodName, #conditionGroup.id}")
	public <C extends WorkflowCondition> List<C> getStepConditions(
			final Class<C> conditionClass,
			final WorkflowStepConditionGroup conditionGroup) {
		final List<C> result = jpaDao.getList(conditionClass, (root, query, cb) -> {
			jpaDao.applyGraph(root, jpaDao.getEntityGraph(conditionClass, "cache"));
			return cb.equal(
					root.get(WorkflowCondition_.stepConditionGroup), conditionGroup
			);
		});
		result.sort(comparing(WorkflowCondition::getSeq, nullsFirst(naturalOrder())));
		return result;
	}

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = CacheConfig.WORKFLOW_CACHE, key = "{#root.methodName, #stepField.id}")
	public <C extends WorkflowCondition> List<C> getFieldConditions(
			final Class<C> conditionClass,
			final WorkflowStepField stepField) {
		final List<C> result = jpaDao.getList(conditionClass, (root, query, cb) -> {
			jpaDao.applyGraph(root, jpaDao.getEntityGraph(conditionClass, "cache"));
			return cb.equal(
					root.get(WorkflowCondition_.stepField), stepField
			);
		});
		result.sort(comparing(WorkflowCondition::getSeq, nullsFirst(naturalOrder())));
		return result;
	}

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = CacheConfig.WORKFLOW_CACHE, key = "{#root.methodName, #childBcAvailability.id}")
	public <C extends WorkflowCondition> List<C> getAvailabilityConditions(
			final Class<C> conditionClass,
			final WorkflowTaskChildBcAvailability childBcAvailability) {
		final List<C> result = jpaDao.getList(conditionClass, (root, query, cb) -> {
			jpaDao.applyGraph(root, jpaDao.getEntityGraph(conditionClass, "cache"));
			return cb.equal(
					root.get(WorkflowCondition_.wfChildBcAvailability), childBcAvailability
			);
		});
		result.sort(comparing(WorkflowCondition::getSeq, nullsFirst(naturalOrder())));
		return result;
	}

}
