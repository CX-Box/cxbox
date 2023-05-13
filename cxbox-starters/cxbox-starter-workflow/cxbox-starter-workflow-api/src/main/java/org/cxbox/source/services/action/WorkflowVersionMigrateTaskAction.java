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

package org.cxbox.source.services.action;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.MessageType;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.core.service.action.ResponseServiceAction;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.engine.workflow.dao.WorkflowableTaskDao;
import org.cxbox.engine.workflow.services.WorkflowDao;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowTransition;
import org.cxbox.model.workflow.entity.WorkflowVersion;
import org.cxbox.model.workflow.entity.WorkflowableTask;
import org.cxbox.source.dto.WorkflowVersionDto;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowVersionMigrateTaskAction extends ResponseServiceAction<WorkflowVersionDto> {

	private final JpaDao jpaDao;

	private final AsyncTaskMigration asyncTaskMigration;

	@Override
	public String getButtonName() {
		return "Мигрировать все задачи и шаблоны на эту версию";
	}

	@Override
	public boolean isAvailable(final BusinessComponent bc) {
		return bc.getId() != null && !jpaDao.findById(WorkflowVersion.class, bc.getIdAsLong()).isDraft();
	}

	@Override
	public ActionResultDTO<WorkflowVersionDto> invoke(final BusinessComponent bc, final WorkflowVersionDto data) {
		asyncTaskMigration.invokeAsync(bc.getIdAsLong());
		return new ActionResultDTO<>(data).setAction(PostAction.showMessage(
				MessageType.INFO,
				errorMessage("info.workflow_migration_has_been_started")
		));
	}

	@Slf4j
	@Service
	@RequiredArgsConstructor
	public static class AsyncTaskMigration {

		private final JpaDao jpaDao;

		private final SessionService sessionService;

		private final WorkflowableTaskDao<?> workflowableTaskDao;

		private final WorkflowDao workflowDao;

		private final TransactionService txService;

		@Async
		void invokeAsync(final Long versionId) {
			try {
				txService.invokeInTx(Invoker.of(() -> doInvoke(versionId)));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		private void doInvoke(Long versionId) {
			final WorkflowVersion version = jpaDao.findById(WorkflowVersion.class, versionId);
			final List<? extends WorkflowableTask> tasks = workflowableTaskDao.getOtherVersionTasks(version);

			int migrated = 0;
			int skipped = 0;
			for (final WorkflowableTask task : tasks) {
				final WorkflowTransition newAutomaticTransition = getNewAutomaticTransition(version, task);
				final WorkflowStep newStep = getNewStep(version, task, newAutomaticTransition);
				boolean shouldSkip = (task.getWorkflowTask() != null && newStep == null)
						|| (task.getAutomaticTransitionName() != null && newAutomaticTransition == null);
				if (shouldSkip) {
					skipped++;
					continue;
				}
				workflowDao.setWorkflowStep(task.getWorkflowTask(), newStep);
				task.setAutomaticTransitionName(
						Optional.ofNullable(newAutomaticTransition).map(WorkflowTransition::getName).orElse(null)
				);
				migrated++;
			}
		}


		private WorkflowTransition getNewAutomaticTransition(final WorkflowVersion version, final WorkflowableTask task) {
			if (task.getAutomaticTransitionName() == null) {
				return null;
			}
			return workflowDao.getTransitionByName(version, task.getAutomaticTransitionName());
		}

		private WorkflowStep getNewStep(final WorkflowVersion version, final WorkflowableTask task,
				final WorkflowTransition newAutomaticTransition) {
			if (newAutomaticTransition != null) {
				return newAutomaticTransition.getSourceStep();
			} else if (task.getWorkflowTask() != null) {
				return workflowDao.getStepByName(version, task.getWorkflowTask().getStepName());
			} else {
				return null;
			}
		}

	}

}
