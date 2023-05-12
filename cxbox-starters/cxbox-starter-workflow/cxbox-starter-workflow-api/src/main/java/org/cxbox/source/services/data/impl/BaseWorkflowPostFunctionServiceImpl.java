
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
import org.cxbox.engine.workflow.services.WorkflowDao;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.workflow.entity.WorkflowPostFunction;
import org.cxbox.model.workflow.entity.WorkflowPostFunction_;
import org.cxbox.model.workflow.entity.WorkflowTransitionConditionGroup;
import org.cxbox.model.workflow.entity.WorkflowTransitionConditionGroup_;
import org.cxbox.source.dto.WorkflowPostFunctionDto;
import org.cxbox.source.dto.WorkflowPostFunctionDto_;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.metamodel.SingularAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

public abstract class BaseWorkflowPostFunctionServiceImpl<D extends WorkflowPostFunctionDto, E extends WorkflowPostFunction> extends
		VersionAwareResponseService<D, E> {

	@Autowired
	private WorkflowDao workflowDao;

	public BaseWorkflowPostFunctionServiceImpl(
			final Class<D> typeOfDTO,
			final Class<E> typeOfEntity,
			final SingularAttribute<? super E, ? extends BaseEntity> parentSpec,
			final Class<? extends FieldMetaBuilder<D>> metaBuilder) {
		super(typeOfDTO, typeOfEntity, parentSpec, metaBuilder);
	}

	@Override
	protected final ActionResultDTO<D> doUpdateEntity(E entity, D dto, BusinessComponent bc) {
		update(entity, dto, bc);
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	protected void update(E entity, D dto, BusinessComponent bc) {
		if (dto.isFieldChanged(WorkflowPostFunctionDto_.seq)) {
			entity.setSeq(dto.getSeq());
		}
		if (dto.isFieldChanged(WorkflowPostFunctionDto_.actionCd)) {
			entity.setActionCd(WorkflowDictionaryType.WF_TRN_ACT.lookupName(dto.getActionCd()));
		}
		if (dto.isFieldChanged(WorkflowPostFunctionDto_.stepTerm)) {
			entity.setStepTerm(dto.getStepTerm());
		}
	}

	@Override
	protected final CreateResult<D> doCreateEntity(final E entity, final BusinessComponent bc) {
		entity.setConditionGroup(
				baseDAO.findById(WorkflowTransitionConditionGroup.class, bc.getParentIdAsLong())
		);
		Optional<WorkflowPostFunction> workflowPostFunction = baseDAO.getList(
				WorkflowPostFunction.class,
				Specification.where((root, cq, cb) -> cb.equal(
						root.get(WorkflowPostFunction_.conditionGroup).get(WorkflowTransitionConditionGroup_.id),
						bc.getParentIdAsLong()
				))
		).stream().filter(o -> !Objects.isNull(o.getSeq())).max(Comparator.comparing(WorkflowPostFunction::getSeq));
		entity
				.setSeq(workflowPostFunction.map(workflowPostFunction1 -> workflowPostFunction1.getSeq() + 1).orElse(1L));
		entity.setActionCd(WorkflowDictionaryType.WF_TRN_ACT.lookupName("SetStepTerm"));
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	protected abstract E create(BusinessComponent bc);

	@Override
	public ActionResultDTO<D> deleteEntity(final BusinessComponent bc) {
		workflowDao.deletePostFunction(isExist(bc.getIdAsLong()));
		return new ActionResultDTO<>();
	}

	@Override
	public Actions<D> getActions() {
		return Actions.<D>builder()
				.create().available(this::isCreateAvailable).add()
				.save().add()
				.delete().add()
				.build();
	}

	protected boolean isCreateAvailable(BusinessComponent bc) {
		return true;
	}

}
