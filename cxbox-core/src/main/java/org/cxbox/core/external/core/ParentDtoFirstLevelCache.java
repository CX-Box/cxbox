package org.cxbox.core.external.core;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.controller.BCFactory;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.AnySourceBcDescription;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.crudma.state.BcState;
import org.cxbox.core.crudma.state.BcStateAware;
import org.cxbox.core.service.AnySourceResponseFactory;
import org.cxbox.core.service.AnySourceResponseService;
import org.cxbox.core.service.ResponseFactory;
import org.cxbox.core.service.ResponseService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;


/**
 * Stores parent DTO of current request bc.
 * Note! cache do NOT store dto of current bc
 */
@Component
@RequestScope
@RequiredArgsConstructor
public class ParentDtoFirstLevelCache {

	private final ConcurrentHashMap<String, DataResponseDTO> cache = new ConcurrentHashMap<>();

	private final ResponseFactory respFactory;

	private final AnySourceResponseFactory anySourceResponseFactory;

	private final BCFactory bcFactory;

	private final TransactionService transactionService;

	private final BcStateAware bcStateAware;

	private <T extends DataResponseDTO> void putCacheValue(BusinessComponent parentBc, T value) {
		this.cache.put(parentBc.getName(), value);
	}

	public void restoreParentBc(boolean readOnly, BusinessComponent childBc) {
		var parentBc = this.getParentBcForRestore(childBc);
		if (parentBc != null) {
			if (parentBc.getDescription() instanceof AnySourceBcDescription) {
				restoreAnySourceParentBc(readOnly, parentBc);
			}
			if (parentBc.getDescription() instanceof InnerBcDescription) {
				restoreInnerBc(readOnly, parentBc);
			}
		}
	}

	public <P extends DataResponseDTO, F> F getParentField(DtoField<P, F> dtoField, BusinessComponent childBc) {
		P parentDto = getOneParentBcStateAware(childBc);
		return Optional.ofNullable(parentDto).map(dtoField.getGetter()).orElse(null);
	}

	private <P extends DataResponseDTO> P getOneParentBcStateAware(BusinessComponent childBc) {
		var parentDto = this.cache.get(childBc.getParentName());
		if (parentDto == null) {
			parentDto = getOneParent(childBc).orElse(null);
		}
		return (P) parentDto;
	}

	private Optional<? extends DataResponseDTO> getOneParent(BusinessComponent childBc) {
		var parentBc = getParentBcForRestore(childBc);
		if (parentBc == null) {
			return Optional.empty();
		}
		if (parentBc.getDescription() instanceof AnySourceBcDescription) {
			return Optional.ofNullable(getAnySourceResponseService(parentBc).getOne(parentBc));
		}
		if (parentBc.getDescription() instanceof InnerBcDescription) {
			return Optional.ofNullable(getInnerBcResponseService(parentBc).getOne(parentBc));
		}
		return Optional.empty();
	}

	private void restoreAnySourceParentBc(boolean readOnly, BusinessComponent bc) {
		if (bc != null && (bc.getDescription() instanceof AnySourceBcDescription)) {
			final BcState state = bcStateAware.getState(bc);
			if (state != null) {
				if (state.getPendingAction() != null) {
					QueryParameters originalParameters = bc.getParameters();
					originalParameters.setParameter("_action", state.getPendingAction());
					bc.setParameters(originalParameters);
				}
				final AnySourceResponseService<?, ?> responseService = this.getAnySourceResponseService(bc);
				DataResponseDTO parentDto = null;
				if (readOnly) { //restore parent for read only childes operations. For example, we create new task and read reviewers from picklist (that is child for task.
					if (!bcStateAware.isPersisted(bc)) {
						parentDto = responseService.createEntity(bc).getRecord();
					}
					if (state.getDto() != null) { //we always restore parent
						parentDto = responseService.updateEntity(bc, state.getDto()).getRecord();
					}
				} else { //restore parent for non read only childes. For example, we create new task and read reviewers from picklist AND then CREATE new reviewer in popup.
					// So in this case we roll back parent right here and recommend users to use VersionAware.getParentDTO() instead of finding parent in DB by id
					parentDto = transactionService.invokeInNewRollbackOnlyTx(() -> {
						DataResponseDTO result = null;
						if (!bcStateAware.isPersisted(bc)) {
							result = responseService.createEntity(bc).getRecord();
						}
						if (state.getDto() != null) { //we always restore parent
							result = responseService.updateEntity(bc, state.getDto()).getRecord();
						}
						return result;
					});
				}
				this.putCacheValue(bc, parentDto);
			}
		}
	}

	private void restoreInnerBc(boolean readOnly, @Nullable BusinessComponent bc) {
		if (bc != null && (bc.getDescription() instanceof InnerBcDescription)) {
			final BcState state = bcStateAware.getState(bc);
			if (state != null) {
				if (state.getPendingAction() != null) {
					QueryParameters originalParameters = bc.getParameters();
					originalParameters.setParameter("_action", state.getPendingAction());
					bc.setParameters(originalParameters);
				}
				final ResponseService<?, ?> responseService = this.getInnerBcResponseService(bc);
				DataResponseDTO parentDto = null;
				if (readOnly) { //restore parent for read only childes operations. For example, we create new task and read reviewers from picklist (that is child for task.
					if (!bcStateAware.isPersisted(bc)) {
						parentDto = responseService.createEntity(bc).getRecord();
					}
					if (state.getDto() != null) { //we always restore parent
						parentDto = responseService.updateEntity(bc, state.getDto()).getRecord();
					}
				} else { //restore parent for non read only childes. For example, we create new task and read reviewers from picklist AND then CREATE new reviewer in popup.
					// So in this case we roll back parent right here and recommend users to use VersionAware.getParentDTO() instead of finding parent in DB by id
					parentDto = transactionService.invokeInNewRollbackOnlyTx(() -> {
						DataResponseDTO result = null;
						if (!bcStateAware.isPersisted(bc)) {
							result = responseService.createEntity(bc).getRecord();
						}
						if (state.getDto() != null) { //we always restore parent
							result = responseService.updateEntity(bc, state.getDto()).getRecord();
						}
						return result;
					});
				}
				this.putCacheValue(bc, parentDto);
			}
		}
	}

	@Nullable
	public BusinessComponent getParentBcForRestore(@NonNull final BusinessComponent childBc) {
		if (childBc.getHierarchy() == null || childBc.getHierarchy().getParent() == null) {
			return null;
		}
		return bcFactory.getBusinessComponent(
				childBc.getHierarchy().getParent(),
				QueryParameters.onlyDatesQueryParameters(
						childBc.getParameters()
				)
		);
	}

	public AnySourceResponseService<?, ?> getAnySourceResponseService(BusinessComponent bc) {
		return anySourceResponseFactory.getService(bc.getDescription());
	}

	public ResponseService<?, ?> getInnerBcResponseService(BusinessComponent bc) {
		return respFactory.getService(bc.getDescription());
	}

}
