
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

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dict.WorkflowDictionaryType;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.core.entity.Department;
import org.cxbox.model.workflow.entity.WorkflowAssigneeRecommendation;
import org.cxbox.model.workflow.entity.WorkflowStepConditionGroup;
import org.cxbox.source.dto.WorkflowAssigneeRecommendationDto;
import org.cxbox.source.dto.WorkflowAssigneeRecommendationDto_;
import javax.persistence.metamodel.SingularAttribute;


public abstract class BaseWorkflowAssigneeRecommendationServiceImpl<D extends WorkflowAssigneeRecommendationDto, E extends WorkflowAssigneeRecommendation> extends
		VersionAwareResponseService<D, E> {

	public BaseWorkflowAssigneeRecommendationServiceImpl(
			final Class<D> typeOfDTO,
			final Class<E> typeOfEntity,
			final SingularAttribute<? super E, ? extends BaseEntity> parentSpec,
			final Class<? extends FieldMetaBuilder<D>> metaBuilder) {
		super(typeOfDTO, typeOfEntity, parentSpec, metaBuilder);
	}

	@Override
	protected final CreateResult<D> doCreateEntity(final E entity, final BusinessComponent bc) {
		entity.setConditionGroup(baseDAO.findById(WorkflowStepConditionGroup.class, bc.getParentIdAsLong()));
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	protected abstract E create(BusinessComponent bc);

	@Override
	protected final ActionResultDTO<D> doUpdateEntity(E entity, D dto, BusinessComponent bc) {
		update(entity, dto, bc);
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	protected void update(E entity, D dto, BusinessComponent bc) {
		if (dto.isFieldChanged(WorkflowAssigneeRecommendationDto_.condAssigneeCd)) {
			entity.setCondAssigneeCd(WorkflowDictionaryType.WF_COND_ASSIGNEE.lookupName(dto.getCondAssigneeCd()));
		}
		if (dto.isFieldChanged(WorkflowAssigneeRecommendationDto_.departmentId)) {
			entity.setDepartment(
					dto.getDepartmentId() == null ? null : baseDAO.findById(Department.class, dto.getDepartmentId()));
		}
		if (dto.isFieldChanged(WorkflowAssigneeRecommendationDto_.description)) {
			entity.setDescription(dto.getDescription());
		}
	}

	@Override
	public Actions<D> getActions() {
		return Actions.<D>builder()
				.create().add()
				.save().add()
				.delete().add()
				.build();
	}

}
