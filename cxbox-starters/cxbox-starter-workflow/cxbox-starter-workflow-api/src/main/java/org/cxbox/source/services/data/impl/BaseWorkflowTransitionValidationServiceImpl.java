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

import static java.util.Objects.nonNull;

import org.cxbox.WorkflowServiceAssociation;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dict.WorkflowDictionaryType;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.workflow.entity.WorkflowTransitionConditionGroup;
import org.cxbox.model.workflow.entity.WorkflowTransitionValidation;
import org.cxbox.model.workflow.entity.WorkflowTransitionValidation_;
import org.cxbox.source.dto.WorkflowTransitionValidationDto;
import org.cxbox.source.dto.WorkflowTransitionValidationDto_;
import javax.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;


public abstract class BaseWorkflowTransitionValidationServiceImpl<D extends WorkflowTransitionValidationDto, E extends WorkflowTransitionValidation> extends
		VersionAwareResponseService<D, E> {

	public BaseWorkflowTransitionValidationServiceImpl(
			final Class<D> typeOfDTO,
			final Class<E> typeOfEntity,
			final SingularAttribute<? super E, ? extends BaseEntity> parentSpec,
			final Class<? extends FieldMetaBuilder<D>> metaBuilder) {
		super(typeOfDTO, typeOfEntity, parentSpec, metaBuilder);
	}

	@Override
	protected Specification<E> getParentSpecification(BusinessComponent bc) {
		if (WorkflowServiceAssociation.wfTransitionValidPreInvoke.isBc(bc)) {
			return (root, cq, cb) -> {
				Long id = bc.getParentIdAsLong();
				if (nonNull(id)) {
					return cb.equal(root.get(WorkflowTransitionValidation_.id), id);
				} else {
					return cb.and();
				}
			};
		}
		return super.getParentSpecification(bc);
	}

	@Override
	protected final ActionResultDTO<D> doUpdateEntity(E entity, D dto, BusinessComponent bc) {
		update(entity, dto, bc);
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	protected void update(E entity, D dto, BusinessComponent bc) {
		if (dto.isFieldChanged(WorkflowTransitionValidationDto_.seq)) {
			entity.setSeq(dto.getSeq());
		}
		if (dto.isFieldChanged(WorkflowTransitionValidationDto_.dmn)) {
			entity.setDmn(dto.getDmn());
		}
		if (dto.isFieldChanged(WorkflowTransitionValidationDto_.validCd)) {
			entity.setValidCd(WorkflowDictionaryType.WF_TRN_DATA_VAL.lookupName(dto.getValidCd()));
		}
		if (dto.isFieldChanged(WorkflowTransitionValidationDto_.errorMessage)) {
			entity.setErrorMessage(dto.getErrorMessage());
		}
		if (dto.isFieldChanged(WorkflowTransitionValidationDto_.preInvokeType)) {
			entity.setPreInvokeTypeCd(WorkflowDictionaryType.PRE_INVOKE_TYPE.lookupName(dto.getPreInvokeType()));
		}
		if (dto.isFieldChanged(WorkflowTransitionValidationDto_.preInvokeCond)) {
			entity.setPreInvokeCondCd(WorkflowDictionaryType.PRE_INVOKE_COND.lookupName(dto.getPreInvokeCond()));
		}
		if (dto.isFieldChanged(WorkflowTransitionValidationDto_.preInvokeMessage)) {
			entity.setPreInvokeMessage(dto.getPreInvokeMessage());
		}
	}

	@Override
	public ActionResultDTO<D> deleteEntity(BusinessComponent bc) {
		baseDAO.delete(WorkflowTransitionValidation.class, bc.getIdAsLong());
		return new ActionResultDTO<>();
	}

	@Override
	protected final CreateResult<D> doCreateEntity(final E entity, final BusinessComponent bc) {
		entity.setConditionGroup(
				baseDAO.findById(WorkflowTransitionConditionGroup.class, bc.getParentIdAsLong())
		);
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	protected abstract E create(BusinessComponent bc);

	@Override
	public Actions<D> getActions() {
		return Actions.<D>builder()
				.create().add()
				.save().add()
				.delete().add()
				.build();
	}

}
