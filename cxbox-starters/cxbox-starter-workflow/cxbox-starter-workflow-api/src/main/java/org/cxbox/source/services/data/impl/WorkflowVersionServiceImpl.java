
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

import static org.cxbox.core.service.action.ActionAvailableChecker.and;
import static org.apache.commons.lang3.StringUtils.joinWith;

import org.cxbox.WorkflowServiceAssociation;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.DrillDownType;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionType;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.core.service.action.Actions;
import org.cxbox.engine.workflow.services.WorkflowDao;
import org.cxbox.engine.workflow.services.WorkflowExporter;
import org.cxbox.model.core.entity.BaseEntity_;
import org.cxbox.api.file.entity.CxboxFile;
import org.cxbox.model.workflow.entity.Workflow;
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowVersion;
import org.cxbox.model.workflow.entity.WorkflowVersion_;
import org.cxbox.source.dto.WorkflowVersionDto;
import org.cxbox.source.dto.WorkflowVersionDto_;
import org.cxbox.source.services.action.WorkflowVersionMigrateTaskAction;
import org.cxbox.source.services.data.WorkflowVersionService;
import org.cxbox.source.services.meta.WorkflowVersionFieldMetaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowVersionServiceImpl extends
		VersionAwareResponseService<WorkflowVersionDto, WorkflowVersion> implements WorkflowVersionService {

	@Autowired(required = false)
	private WorkflowExporter workflowExporter;

	@Autowired
	private WorkflowDao workflowDao;

	@Autowired
	private WorkflowVersionMigrateTaskAction workflowVersionMigrateTaskAction;

	public WorkflowVersionServiceImpl() {
		super(
				WorkflowVersionDto.class,
				WorkflowVersion.class,
				WorkflowVersion_.workflow,
				WorkflowVersionFieldMetaBuilder.class
		);
	}

	@Override
	protected Specification<WorkflowVersion> getParentSpecification(BusinessComponent bc) {
		if (WorkflowServiceAssociation.wfActiveVersion.isBc(bc)) {
			return (root, query, cb) -> cb.and(
					cb.equal(root.get(WorkflowVersion_.workflow).get(BaseEntity_.id), bc.getParentIdAsLong()),
					cb.equal(root.get(WorkflowVersion_.draft), Boolean.FALSE)
			);
		}
		return super.getParentSpecification(bc);
	}

	@Override
	protected ActionResultDTO<WorkflowVersionDto> doUpdateEntity(WorkflowVersion entity, WorkflowVersionDto dto,
			BusinessComponent bc) {
		if (dto.isFieldChanged(WorkflowVersionDto_.description)) {
			entity.setDescription(dto.getDescription());
		}
		if (dto.isFieldChanged(WorkflowVersionDto_.firstStepId)) {
			entity.setFirstStep(
					dto.getFirstStepId() == null ? null : baseDAO.findById(WorkflowStep.class, dto.getFirstStepId())
			);
		}
		if (dto.isFieldChanged(WorkflowVersionDto_.autoClosedStepId)) {
			entity.setAutoClosedStep(
					dto.getAutoClosedStepId() == null ? null : baseDAO.findById(WorkflowStep.class, dto.getAutoClosedStepId())
			);
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	@Override
	protected CreateResult<WorkflowVersionDto> doCreateEntity(final WorkflowVersion entity,
			final BusinessComponent bc) {
		final Workflow workflow = baseDAO.findById(Workflow.class, bc.getParentIdAsLong());
		entity.setWorkflow(workflow);
		entity.setVersion(workflowDao.getNextVersion(workflow, false).doubleValue());
		entity.setDraft(Boolean.TRUE);
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	public Actions<WorkflowVersionDto> getActions() {
		return Actions.<WorkflowVersionDto>builder()
				.create().available(this::isEditable).add()
				.save().available(this::isEditable).add()
				.action(ActionType.COPY).available(and(
						(bc) -> workflowExporter != null,
						this::versionIsNotDraft
				)).invoker(this::copyNewVersion).add()
				.action("export", "Экспорт для поставки").available(and(
						(bc) -> workflowExporter != null,
						this::versionIsNotDraft
				)).invoker(this::exportNewVersion).add()
				.action("activate-version", "Зафиксировать версию")
				.available(this::versionIsDraft).invoker(this::activateVersion).add()
				.add("migrate-all-tasks", workflowVersionMigrateTaskAction)
				.build();
	}

	private ActionResultDTO<WorkflowVersionDto> exportNewVersion(BusinessComponent bc, WorkflowVersionDto data) {
		final CxboxFile fileEntity = workflowExporter.exportNewVersion(bc, data);
		return new ActionResultDTO<>(data).setAction(PostAction.downloadFile(String.valueOf(fileEntity.getId())));
	}

	private ActionResultDTO<WorkflowVersionDto> copyNewVersion(BusinessComponent bc, WorkflowVersionDto data) {
		final WorkflowVersion newVersion = workflowExporter.copyNewVersion(bc, data);
		return new ActionResultDTO<>(data).setAction(PostAction.drillDown(
				DrillDownType.INNER,
				joinWith(
						"/",
						"screen/admin/view/wftransitionfunc",
						WorkflowServiceAssociation.wf,
						newVersion.getWorkflow().getId(),
						WorkflowServiceAssociation.wfVersion,
						newVersion.getId()
				)
		));
	}

	private boolean isEditable(final BusinessComponent bc) {
		return WorkflowServiceAssociation.migrationWfVersion.isNotBc(bc);
	}

	private boolean versionIsNotDraft(final BusinessComponent bc) {
		return isEditable(bc) && bc.getId() != null && !baseDAO.findById(WorkflowVersion.class, bc.getIdAsLong()).isDraft();
	}

	private boolean versionIsDraft(final BusinessComponent bc) {
		return isEditable(bc) && bc.getId() != null && baseDAO.findById(WorkflowVersion.class, bc.getIdAsLong()).isDraft();
	}

	private ActionResultDTO<WorkflowVersionDto> activateVersion(final BusinessComponent bc,
			final WorkflowVersionDto data) {
		final WorkflowVersion version = baseDAO.findById(WorkflowVersion.class, bc.getIdAsLong());
		version.setDraft(false);
		return new ActionResultDTO<>(entityToDto(bc, version));
	}

}
