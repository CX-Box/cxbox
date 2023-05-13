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

package org.cxbox.source.services.data.impl;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import org.cxbox.WorkflowServiceAssociation;
import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.AbstractResponseService;
import org.cxbox.core.dao.BaseDAO;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.service.action.Actions;
import org.cxbox.engine.workflow.WorkflowSettings;
import org.cxbox.engine.workflow.dao.WorkflowableTaskDao;
import org.cxbox.engine.workflow.services.WorkflowDao;
import org.cxbox.engine.workflow.services.WorkflowEngine;
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowTask_;
import org.cxbox.model.workflow.entity.WorkflowTransition;
import org.cxbox.model.workflow.entity.WorkflowVersion;
import org.cxbox.model.workflow.entity.WorkflowableTask;
import org.cxbox.model.workflow.entity.WorkflowableTask_;
import org.cxbox.source.dto.WorkflowTaskMigrationDto;
import org.cxbox.source.dto.WorkflowTaskMigrationDto_;
import org.cxbox.source.services.data.WorkflowTaskMigrationService;
import org.cxbox.source.services.meta.WorkflowTaskMigrationFieldMetaBuilder;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTaskMigrationServiceImpl extends
		AbstractResponseService<WorkflowTaskMigrationDto, WorkflowableTask> implements WorkflowTaskMigrationService {

	@Autowired
	private WorkflowSettings<?> workflowSettings;

	@Autowired
	private WorkflowableTaskDao<?> workflowableTaskDao;

	@Autowired
	private WorkflowEngine workflowEngine;

	@Autowired
	private WorkflowDao workflowDao;

	public WorkflowTaskMigrationServiceImpl() {
		super(WorkflowTaskMigrationDto.class, WorkflowableTask.class, null, WorkflowTaskMigrationFieldMetaBuilder.class);
	}

	@Override
	protected String getFetchGraphName(BusinessComponent bc) {
		return null;
	}

	@Override
	public WorkflowableTask getOneAsEntity(final BusinessComponent bc) {
		return workflowableTaskDao.getTask(bc.getIdAsLong());
	}

	@Override
	public ResultPage<WorkflowTaskMigrationDto> getList(final BaseDAO dao, final BusinessComponent bc) {
		return getList(dao, bc, (Class<WorkflowableTask>) workflowSettings.getEntityClass(), typeOfDTO);
	}

	@Override
	public long count(final BaseDAO dao, final BusinessComponent bc) {
		return count(dao, bc, (Class<WorkflowableTask>) workflowSettings.getEntityClass(), typeOfDTO);
	}

	@Override
	protected Specification<WorkflowableTask> getParentSpecification(final BusinessComponent bc) {
		final WorkflowVersion version = baseDAO.findById(WorkflowVersion.class, bc.getParentIdAsLong());
		return (root, query, cb) -> cb.and(
				cb.equal(
						root.get(WorkflowableTask_.workflowTask)
								.get(WorkflowTask_.workflowName),
						version.getWorkflow().getName()
				),
				cb.equal(
						root.get(WorkflowableTask_.workflowTask).get(WorkflowTask_.version),
						version.getVersion()
				),
				cb.equal(
						root.get(WorkflowableTask_.templateFlg),
						WorkflowServiceAssociation.wfTemplateMigration.isBc(bc) ? Boolean.TRUE : Boolean.FALSE
				)
		);
	}

	@Override
	public ActionResultDTO<WorkflowTaskMigrationDto> updateEntity(final BusinessComponent bc,
			final DataResponseDTO data) {
		final WorkflowableTask entity = workflowableTaskDao.getTask(bc.getIdAsLong());
		final WorkflowTaskMigrationDto dto = (WorkflowTaskMigrationDto) data;
		if (data.hasChangedFields()) {
			if (data.isFieldChanged(WorkflowTaskMigrationDto_.newStepId)) {
				workflowEngine.setCustomStep(entity, baseDAO.findById(WorkflowStep.class, dto.getNewStepId()));
			}
			if (data.isFieldChanged(WorkflowTaskMigrationDto_.newAutomaticTransitionId)) {
				final WorkflowTransition automaticTransition = workflowDao.getActiveWorkflowTransitionByName(
						dto.getNewAutomaticTransitionId()
				);
				if (!Objects.equals(automaticTransition.getSourceStep(), workflowDao.getWorkflowStep(entity.getWorkflowTask()))) {
					throw new BusinessException().addPopup(errorMessage("error.automatic_transition_mismatch"));
				}
				entity.setAutomaticTransitionName(automaticTransition.getName());
			}
		}
		final WorkflowTaskMigrationDto updatedDto = entityToDto(bc, entity);
		return new ActionResultDTO<>(updatedDto).setAction(PostAction.refreshBc(bc));
	}

	@Override
	public Actions<WorkflowTaskMigrationDto> getActions() {
		return Actions.<WorkflowTaskMigrationDto>builder()
				.save().add()
				.build();
	}

}
