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

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import org.cxbox.WorkflowServiceAssociation;
import org.cxbox.api.data.dictionary.DictionaryType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.engine.workflow.services.WorkflowDao;
import org.cxbox.model.core.entity.Department;
import org.cxbox.model.workflow.entity.Workflow;
import org.cxbox.model.workflow.entity.WorkflowVersion;
import org.cxbox.source.dto.WorkflowDto;
import org.cxbox.source.dto.WorkflowDto_;
import org.cxbox.source.services.data.WorkflowService;
import org.cxbox.source.services.meta.WorkflowFieldMetaBuilder;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class WorkflowServiceImpl extends VersionAwareResponseService<WorkflowDto, Workflow> implements WorkflowService {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private WorkflowDao workflowDao;

	public WorkflowServiceImpl() {
		super(WorkflowDto.class, Workflow.class, null, WorkflowFieldMetaBuilder.class);
	}

	@Override
	protected Specification<Workflow> getParentSpecification(BusinessComponent bc) {
		if (WorkflowServiceAssociation.pfChildWorkflow.isBc(bc)) {
			return (root, cq, cb) -> cb.and();
		}
		return super.getParentSpecification(bc);
	}

	@Override
	protected ActionResultDTO<WorkflowDto> doUpdateEntity(Workflow entity, WorkflowDto dto, BusinessComponent bc) {
		if (dto.isFieldChanged(WorkflowDto_.deptId)) {
			entity.setDepartment(baseDAO.findById(Department.class, dto.getDeptId()));
		}
		if (dto.isFieldChanged(WorkflowDto_.name)) {
			entity.setName(dto.getName());
		}
		if (dto.isFieldChanged(WorkflowDto_.description)) {
			entity.setDescription(dto.getDescription());
		}
		if (dto.isFieldChanged(WorkflowDto_.taskTypeCd)) {
			entity.setTaskTypeCd(DictionaryType.TASK_TYPE.lookupName(dto.getTaskTypeCd()));
		}
		if (dto.isFieldChanged(WorkflowDto_.activeVersion)) {
			entity.setActiveVersion(
					dto.getActiveVersionId() == null ? null : baseDAO.findById(WorkflowVersion.class, dto.getActiveVersionId())
			);
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	@Override
	public ActionResultDTO<WorkflowDto> deleteEntity(BusinessComponent bc) {
		baseDAO.delete(Workflow.class, bc.getIdAsLong());
		return new ActionResultDTO<>();
	}

	@Override
	protected CreateResult<WorkflowDto> doCreateEntity(final Workflow entity, final BusinessComponent bc) {
		List<LOV> taskTypes = workflowDao.getTaskTypesNotInWf();
		if (taskTypes.isEmpty()) {
			throw new BusinessException().addPopup(errorMessage("error.no_task_type_available"));
		}
		entity.setTaskTypeCd(taskTypes.get(0));
		entity.setDepartment(sessionService.getSessionUserDepartment());
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	public Actions<WorkflowDto> getActions() {
		return Actions.<WorkflowDto>builder()
				.create().available(this::hasTaskTypesNotInWf).add()
				.save().available(this::isEditable).add()
				.delete().available(this::isEditable).add()
				.build();
	}

	private boolean hasTaskTypesNotInWf(final BusinessComponent bc) {
		return isEditable(bc) && !workflowDao.getTaskTypesNotInWf().isEmpty();
	}

	private boolean isEditable(final BusinessComponent bc) {
		return WorkflowServiceAssociation.migrationWf.isNotBc(bc);
	}

}
