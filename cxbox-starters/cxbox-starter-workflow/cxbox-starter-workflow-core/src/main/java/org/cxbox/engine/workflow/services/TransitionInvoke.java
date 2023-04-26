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

package org.cxbox.engine.workflow.services;

import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.core.util.DateTimeUtil;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.engine.workflow.dao.WorkflowDaoImpl;
import org.cxbox.engine.workflow.dao.WorkflowableTaskDao;
import org.cxbox.model.core.entity.User;
import org.cxbox.model.workflow.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.cxbox.api.data.dictionary.CoreDictionaries.TaskStatusCategory.isAutoClosed;
import static org.cxbox.api.data.dictionary.CoreDictionaries.TaskStatusCategory.isDone;

@Slf4j
@Service
@RequiredArgsConstructor
final class TransitionInvoke {

	private final SessionService sessionService;

	private final Optional<StatusCategoryService> statusCategoryService;

	private final WorkflowableTaskDao<?> workflowableTaskDao;

	private final Optional<ObserverService> observerService;

	private final PostFunctionExecute postFunctionExecute;

	private final WorkflowDaoImpl workflowDao;

	TransitionResult invoke(final BcDescription bcDescription, final WorkflowableTask task,
			final WorkflowTransition transition) {
		final TransitionResult transitionResult = isBackgroundExecution(transition)
				? waitTransition(task, transition)
				: forceInvoke(bcDescription, task, transition, false);

		if (bcDescription != null
				&& isFinalStep(transition.getDestinationStep())
				&& !isAutoClosedStep(transition.getDestinationStep())
				&& workflowableTaskDao.isClosedChild(task)) {
			transitionResult.replacePostActions(Collections.singletonList(PostAction.refreshBc(bcDescription)));
		}

		return transitionResult;
	}

	TransitionResult forceInvoke(final BcDescription bcDescription, final WorkflowableTask task,
			final WorkflowTransition transition, final boolean ignorePostFunctions) {
		final User previousAssignee = task.getAssignee();
		observerService.ifPresent(observerService -> observerService.addUserAsObserver(task, previousAssignee));

		final List<PostAction> postActions = ignorePostFunctions ? Collections.emptyList()
				: postFunctionExecute.execute(bcDescription, task, transition);
		final WorkflowTransitionHistory transitionHistory = workflowDao.saveTransitionHistory(
				task,
				transition,
				sessionService.getSessionUser(),
				previousAssignee
		);

		updateTaskWorkflowStep(task, transition.getDestinationStep());
		log.debug("Transition performed successfully");
		return new TransitionResult(transitionHistory, postActions);
	}

	private TransitionResult waitTransition(final WorkflowableTask task, final WorkflowTransition transition) {
		log.debug("Transition will be performed in background mode");
		task.getWorkflowTask().setPendingTransition(workflowDao.createPendingTransition(
				transition,
				sessionService.getSessionUser(),
				sessionService.getSessionUserRole()
		));
		updateTaskWorkflowStep(task, transition.getDestinationStep());
		return new TransitionResult(null, Collections.emptyList());
	}

	private boolean isBackgroundExecution(final WorkflowTransition transition) {
		return BooleanUtils.isTrue(transition.getBackgroundExecution());
	}

	private void updateTaskWorkflowStep(final WorkflowableTask task, final WorkflowStep destinationStep) {
		if (task.getWorkflowTask() == null) {
			task.setWorkflowTask(workflowDao.createWorkflowTask(destinationStep));
		} else {
			workflowDao.setWorkflowStep(task.getWorkflowTask(), destinationStep);
		}

		if (isInProgressStep(destinationStep)) {
			task.setStartDateFact(DateTimeUtil.now());
		} else if (isFinalStep(destinationStep)) {
			task.setResolutionDate(DateTimeUtil.now());
			closeRelatedTasks(task);
		}
	}

	private void closeRelatedTasks(final WorkflowableTask task) {
		for (WorkflowableTask childTask : workflowableTaskDao.findAllLinksWithAutoClosed(task)) {
			final WorkflowTask workflowTask = childTask.getWorkflowTask();
			if (workflowTask != null) {
				final WorkflowStep childStep = workflowDao.getWorkflowStep(workflowTask);
				if (childStep != null && !isFinalStep(childStep)) {
					final WorkflowStep autoClosedStep = childStep.getWorkflowVersion().getAutoClosedStep();
					if (autoClosedStep != null) {
						final WorkflowTransition transition = workflowDao.getTransitionBetweenSteps(childStep, autoClosedStep);
						if (transition != null) {
							invoke(null, childTask, transition);
						} else {
							updateTaskWorkflowStep(childTask, autoClosedStep);
						}
					}
				}
			}
		}
	}

	private boolean isFinalStep(final WorkflowStep step) {
		return statusCategoryService
				.map(service -> isDone(service.getCategory(step.getLinkedStatusCd())))
				.orElse(false);
	}

	private boolean isAutoClosedStep(final WorkflowStep step) {
		return statusCategoryService
				.map(service -> isAutoClosed(service.getCategory(step.getLinkedStatusCd())))
				.orElse(false);
	}

	//todo
	private boolean isInProgressStep(final WorkflowStep step) {
		return "In progress".equals(step.getName());
	}

}
