
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

import static java.util.Optional.ofNullable;

import org.cxbox.WorkflowServiceAssociation;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.engine.workflow.dao.WorkflowableTaskDao;
import org.cxbox.engine.workflow.services.WorkflowDao;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.core.entity.BaseEntity_;
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowStep_;
import org.cxbox.model.workflow.entity.WorkflowTransition;
import org.cxbox.model.workflow.entity.WorkflowVersion;
import org.cxbox.model.workflow.entity.WorkflowVersion_;
import org.cxbox.model.workflow.entity.WorkflowableTask;
import org.cxbox.source.dto.WorkflowStepDto;
import org.cxbox.source.dto.WorkflowStepDto_;
import org.cxbox.source.services.data.WorkflowStepService;
import org.cxbox.source.services.meta.WorkflowStepFieldMetaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class WorkflowStepServiceImpl extends VersionAwareResponseService<WorkflowStepDto, WorkflowStep> implements
		WorkflowStepService {

	@Autowired
	private WorkflowableTaskDao<?> workflowableTaskDao;

	@Autowired
	private WorkflowDao workflowDao;

	public WorkflowStepServiceImpl() {
		super(WorkflowStepDto.class, WorkflowStep.class, WorkflowStep_.workflowVersion, WorkflowStepFieldMetaBuilder.class);
	}

	@Override
	protected Specification<WorkflowStep> getParentSpecification(BusinessComponent bc) {
		if (WorkflowServiceAssociation.wfStepAutoClosed.isBc(bc)) {
			return (root, query, cb) -> cb.and(
					cb.equal(root.get(WorkflowStep_.workflowVersion).get(BaseEntity_.id), bc.getParentIdAsLong()),
					cb.equal(root.get(WorkflowStep_.linkedStatusCd), new LOV("AUTO_CLOSED"))
			);
		} else if (
				WorkflowServiceAssociation.wfTaskMigrationNewStep.isBc(bc)
						|| WorkflowServiceAssociation.wfTemplateMigrationNewStep.isBc(bc)) {
			final WorkflowableTask task = workflowableTaskDao.getTask(bc.getParentIdAsLong());
			return (root, query, cb) -> cb.and(
					cb.equal(
							root.get(WorkflowStep_.workflowVersion).get(WorkflowVersion_.workflow),
							workflowDao.getWorkflowStep(task.getWorkflowTask()).getWorkflowVersion().getWorkflow()
					),
					cb.equal(root.get(WorkflowStep_.workflowVersion).get(WorkflowVersion_.draft), Boolean.FALSE),
					cb.not(cb.equal(
							root.get(WorkflowStep_.workflowVersion),
							workflowDao.getWorkflowStep(task.getWorkflowTask()).getWorkflowVersion()
					))
			);
		}
		return (root, cq, cb) -> {
			final Long parentId = getWorkflowVersionId(bc);
			return parentId == null
					? cb.and()
					: cb.equal(root.get(WorkflowStep_.workflowVersion).get(BaseEntity_.id), parentId);
		};
	}

	protected Long getWorkflowVersionId(BusinessComponent bc) {
		if (WorkflowServiceAssociation.wfTransitionDestStep.isBc(bc)) {
			return ofNullable(baseDAO.findById(WorkflowTransition.class, bc.getParentIdAsLong()))
					.map(WorkflowTransition::getSourceStep)
					.map(WorkflowStep::getWorkflowVersion)
					.map(BaseEntity::getId)
					.orElse(null);
		} else if (
				WorkflowServiceAssociation.wfTaskMigrationCurrentStep.isBc(bc)
						|| WorkflowServiceAssociation.wfTemplateMigrationCurrentStep.isBc(bc)) {
			return ofNullable(workflowableTaskDao.getTask(bc.getParentIdAsLong()))
					.map(WorkflowableTask::getWorkflowTask)
					.map(wfTask -> workflowDao.getWorkflowStep(wfTask))
					.map(WorkflowStep::getWorkflowVersion)
					.map(BaseEntity::getId)
					.orElse(null);
		}
		return bc.getParentIdAsLong();
	}

	@Override
	protected ActionResultDTO<WorkflowStepDto> doUpdateEntity(WorkflowStep entity, WorkflowStepDto dto,
			BusinessComponent bc) {
		if (dto.isFieldChanged(WorkflowStepDto_.name)) {
			entity.setName(dto.getName());
		}
		if (dto.isFieldChanged(WorkflowStepDto_.linkedStatusValue)) {
			entity.setLinkedStatusCd(dto.getLinkedStatusKey() == null ? null : new LOV(dto.getLinkedStatusKey()));
		}
		if (dto.isFieldChanged(WorkflowStepDto_.overdueTransitionId)) {
			entity.setOverdueTransition(
					dto.getOverdueTransitionId() == null
							? null
							: baseDAO.findById(WorkflowTransition.class, dto.getOverdueTransitionId())
			);
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	@Override
	public ActionResultDTO<WorkflowStepDto> deleteEntity(BusinessComponent bc) {
		baseDAO.delete(WorkflowStep.class, bc.getIdAsLong());
		return new ActionResultDTO<>();
	}

	@Override
	protected CreateResult<WorkflowStepDto> doCreateEntity(final WorkflowStep entity, final BusinessComponent bc) {
		entity.setWorkflowVersion(baseDAO.findById(WorkflowVersion.class, bc.getParentIdAsLong()));
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	public Actions<WorkflowStepDto> getActions() {
		return Actions.<WorkflowStepDto>builder()
				.create().add()
				.save().add()
				.delete().add()
				.build();
	}

}
