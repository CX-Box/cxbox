
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

package org.cxbox.engine.workflow.services;


import org.cxbox.core.crudma.bc.BcIdentifier;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.engine.workflow.WorkflowSettings;
import org.cxbox.engine.workflow.dao.WorkflowDaoImpl;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.core.entity.User;
import org.cxbox.model.workflow.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service

@RequiredArgsConstructor
public class WorkflowEngineImpl implements WorkflowEngine {

	private final WorkflowSettings<?> workflowSettings;

	private final JpaDao jpaDao;

	private final ConditionCheck conditionCheck;

	private final TransitionCheck transitionCheck;

	private final TransitionValidate transitionValidate;

	private final TransitionInvoke transitionInvoke;

	private final AssigneeRecommender assigneeRecommender;

	private final WorkflowDaoImpl workflowDao;

	@Override
	public void setInitialStep(final WorkflowableTask task) {
		setCustomStep(task, workflowDao.getInitialStep(task.getTaskType()));
	}

	@Override
	public void setCustomStep(final WorkflowableTask task, final WorkflowStep step) {
		if (task.getWorkflowTask() == null) {
			task.setWorkflowTask(workflowDao.createWorkflowTask(step));
		} else {
			workflowDao.setWorkflowStep(task.getWorkflowTask(), step);
		}
	}

	@Override
	public List<WorkflowTransition> getTransitions(final WorkflowableTask task) {
		log.debug("Getting list of possible transitions for task id: {}", task.getId());
		if (transitionValidate.isPendingTransitionTask(task)) {
			log.debug("Transition is pending, other transitions are unavailable");
			return Collections.emptyList();
		}
		final WorkflowStep currentStep = workflowDao.getCurrentStep(task);
		log.debug("Current step of task '{}' id: {}", currentStep.getName(), currentStep.getId());
		return workflowDao.getTransitions(currentStep).stream()
				.filter(transition -> transitionCheck.isAvailable(task, transition))
				.collect(Collectors.toList());
	}

	@Override
	public TransitionResult invokeTransition(
			final BcDescription bcDescription,
			final WorkflowableTask task,
			final WorkflowTransition transition,
			final List<String> preInvokeParameters) {
		log.debug(
				"Invoking transition '{}' id: {} for task id: {}", transition.getName(), transition.getId(), task.getId()
		);
		transitionValidate.validate(task, transition, true, preInvokeParameters);
		return transitionInvoke.invoke(bcDescription, task, transition);
	}

	@Override
	public TransitionResult invokeAutoTransition(WorkflowableTask task, WorkflowTransition transition) {
		log.debug(
				"Invoking auto transition '{}' id: {} for task id: {}",
				transition.getName(),
				transition.getId(),
				task.getId()
		);
		transitionValidate.validate(task, transition, false, Collections.emptyList());
		return transitionInvoke.invoke(null, task, transition);
	}

	@Override
	public TransitionResult forceInvokeAutoTransitionIgnorePostFunctions(WorkflowTransition transition, WorkflowableTask task) {
		log.debug(
				"Invoking auto transition with ignoring of post functions '{}' id: {} for task id: {}",
				transition.getName(),
				transition.getId(),
				task.getId()
		);
		return transitionInvoke.forceInvoke(null, task, transition, true);
	}

	@Override
	public void forceInvokeAutoTransition(final WorkflowableTask task, final WorkflowTransition transition) {
		transitionInvoke.forceInvoke(null, task, transition, false);
	}

	@Override
	public void forceInvokeAutoTransitionToHiddenStep(final WorkflowableTask task) {
		final WorkflowTask workflowTask = task.getWorkflowTask();
		if (workflowTask != null) {
			final WorkflowStep currentStep = workflowDao.getWorkflowStep(workflowTask);
			final WorkflowStep hiddenStep = workflowDao.getHiddenStep(currentStep.getWorkflowVersion());
			if (hiddenStep != null) {
				final WorkflowTransition transition = workflowDao.getTransitionBetweenSteps(currentStep, hiddenStep);
				transitionInvoke.forceInvoke(null, task, transition, false);
			}
		}
	}

	@Override
	public boolean isChildBcDisabled(final BcIdentifier bcIdentifier, final WorkflowableTask task) {
		final WorkflowStep step = workflowDao.getCurrentStep(task);
		if (step == null) {
			return false;
		}
		return workflowDao.getWorkflowTaskChildBcAvailabilities(step).stream()
				.filter(childBcAvailability -> Objects.equals(childBcAvailability.getBcName(), bcIdentifier.getName()))
				.anyMatch(
						childBcAvailability -> conditionCheck.isAvailable(
								task,
								workflowDao.getConditions(workflowSettings.getConditionExtensionClass(), childBcAvailability),
								null
						)
				);
	}

	@Override
	public List<String> getDisableFields(final WorkflowableTask task) {
		log.debug("Getting list of disabled fields for task id: {}", task.getId());
		final Stream<TaskField> disableFields;
		if (transitionValidate.isPendingTransitionTask(task)) {
			log.debug("Transition is pending, edit is not allowed");
			disableFields = jpaDao.getList(TaskField.class).stream();
		} else {
			final WorkflowStep currentStep = workflowDao.getCurrentStep(task);
			log.debug("Current step of task '{}' id: {}", currentStep.getName(), currentStep.getId());
			final List<WorkflowStepField> stepFields = workflowDao.getStepFields(currentStep);
			if (stepFields.isEmpty()) {
				log.debug("List of uneditable fields is not configured");
			}
			disableFields = stepFields.stream().filter(stepField -> stepField.getTaskField() != null).filter(
					stepField -> {
						log.debug("Checking whether the field is not editable '{}'", stepField.getTaskField().getKey());
						final boolean available = conditionCheck.isAvailable(
								task, workflowDao.getConditions(workflowSettings.getConditionExtensionClass(), stepField), null
						);
						log.debug(available ? "Field is not editable" : "Field is editable");
						return available;
					}
			).map(WorkflowStepField::getTaskField);
		}
		return disableFields.map(TaskField::getKey).collect(Collectors.toList());
	}

	@Override
	public Specification<User> getAssigneeRecommendationSpecification(final WorkflowableTask task) {
		return assigneeRecommender.recommend(task);
	}

	@Override
	public boolean checkRequiredFieldsForTransition(final WorkflowTransition transition) {
		return transition.getCheckRequiredFields();
	}

}
