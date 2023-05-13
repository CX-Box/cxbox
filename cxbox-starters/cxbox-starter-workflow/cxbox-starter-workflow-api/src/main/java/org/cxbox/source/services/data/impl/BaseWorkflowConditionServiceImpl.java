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

import static org.cxbox.source.dto.WorkflowConditionDto_.condCd;
import static org.cxbox.source.dto.WorkflowConditionDto_.dmn;
import static org.cxbox.source.dto.WorkflowConditionDto_.seq;

import org.cxbox.WorkflowServiceAssociation;
import org.cxbox.api.data.dictionary.CoreDictionaries.WorkflowConditionType;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dict.WorkflowDictionaries;
import org.cxbox.core.dict.WorkflowDictionaryType;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.workflow.entity.WorkflowCondition;
import org.cxbox.model.workflow.entity.WorkflowCondition_;
import org.cxbox.model.workflow.entity.WorkflowStepConditionGroup;
import org.cxbox.model.workflow.entity.WorkflowStepConditionGroup_;
import org.cxbox.model.workflow.entity.WorkflowStepField;
import org.cxbox.model.workflow.entity.WorkflowStepField_;
import org.cxbox.model.workflow.entity.WorkflowTaskChildBcAvailability;
import org.cxbox.model.workflow.entity.WorkflowTaskChildBcAvailability_;
import org.cxbox.model.workflow.entity.WorkflowTransitionConditionGroup;
import org.cxbox.model.workflow.entity.WorkflowTransitionConditionGroup_;
import org.cxbox.source.dto.WorkflowConditionDto;
import javax.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;


public abstract class BaseWorkflowConditionServiceImpl<D extends WorkflowConditionDto, E extends WorkflowCondition> extends
		VersionAwareResponseService<D, E> {

	public BaseWorkflowConditionServiceImpl(
			final Class<D> typeOfDTO,
			final Class<E> typeOfEntity,
			final SingularAttribute<? super E, ? extends BaseEntity> parentSpec,
			final Class<? extends FieldMetaBuilder<D>> metaBuilder) {
		super(typeOfDTO, typeOfEntity, parentSpec, metaBuilder);
	}

	@Override
	protected Specification<E> getParentSpecification(BusinessComponent bc) {
		final Long parentId = bc.getParentIdAsLong();
		if (WorkflowServiceAssociation.wfStepCondRecommendedAssignee.isBc(bc)) {
			return (root, cq, cb) -> cb.equal(
					root.get(WorkflowCondition_.stepConditionGroup).get(WorkflowStepConditionGroup_.id), parentId
			);
		} else if (WorkflowServiceAssociation.wfStepFieldCond.isBc(bc)) {
			return (root, cq, cb) -> cb.equal(
					root.get(WorkflowCondition_.stepField).get(WorkflowStepField_.id), parentId
			);
		} else if (WorkflowServiceAssociation.wfChildBcAvailabilityCond.isBc(bc)) {
			return (root, cq, cb) -> cb.equal(
					root.get(WorkflowCondition_.wfChildBcAvailability).get(WorkflowTaskChildBcAvailability_.id), parentId
			);
		} else if (WorkflowServiceAssociation.wfTranCondValid.isBc(bc) || WorkflowServiceAssociation.wfTransitionCond
				.isBc(bc) || WorkflowServiceAssociation.wfPostFuncGroupCond.isBc(bc)) {
			return (root, cq, cb) -> cb.equal(
					root.get(WorkflowCondition_.transitionConditionGroup).get(WorkflowTransitionConditionGroup_.id), parentId
			);
		} else {
			return (root, cq, cb) -> cb.and();
		}
	}

	@Override
	protected final CreateResult<D> doCreateEntity(final E entity, final BusinessComponent bc) {
		if (WorkflowServiceAssociation.wfStepCondRecommendedAssignee.isBc(bc)) {
			entity.setStepConditionGroup(baseDAO.findById(WorkflowStepConditionGroup.class, bc.getParentIdAsLong()));
			entity.setCondLinkType(WorkflowConditionType.STEP_CONDITION);
		} else if (WorkflowServiceAssociation.wfStepFieldCond.isBc(bc)) {
			entity.setStepField(baseDAO.findById(WorkflowStepField.class, bc.getParentIdAsLong()));
			entity.setCondLinkType(WorkflowConditionType.STEP_FIELD_CONDITION);
		} else if (WorkflowServiceAssociation.wfChildBcAvailabilityCond.isBc(bc)) {
			entity.setWfChildBcAvailability(baseDAO.findById(WorkflowTaskChildBcAvailability.class, bc.getParentIdAsLong()));
			entity.setCondLinkType(WorkflowConditionType.CHILD_BC_CONDITION);
		} else if (WorkflowServiceAssociation.wfTranCondValid.isBc(bc) || WorkflowServiceAssociation.wfTransitionCond
				.isBc(bc) || WorkflowServiceAssociation.wfPostFuncGroupCond.isBc(bc)) {
			entity.setTransitionConditionGroup(
					baseDAO.findById(WorkflowTransitionConditionGroup.class, bc.getParentIdAsLong())
			);
			entity.setCondLinkType(WorkflowConditionType.TRANSITION_CONDITION);
		}
		entity.setCondCd(WorkflowDictionaries.WfCondition.ALWAYS_HIDDEN);
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
		if (dto.isFieldChanged(seq)) {
			entity.setSeq(dto.getSeq());
		}
		if (dto.isFieldChanged(condCd)) {
			entity.setCondCd(WorkflowDictionaryType.WF_COND.lookupName(dto.getCondCd()));
		}
		if (dto.isFieldChanged(dmn)) {
			entity.setDmn(dto.getDmn());
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
