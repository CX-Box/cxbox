/*
 * © OOO "SI IKS LAB", 2022-2024
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

package org.cxbox.core.crudma.ext.impl;

import static org.cxbox.core.crudma.CrudmaActionType.INVOKE;
import static org.cxbox.core.crudma.CrudmaActionType.PREVIEW;
import static org.cxbox.core.crudma.CrudmaActionType.UPDATE;
import static org.cxbox.core.dto.DrillDownType.INNER;
import static org.cxbox.core.dto.rowmeta.PostAction.BasePostActionType.DRILL_DOWN;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.DataResponseDTO_;
import org.cxbox.api.data.dto.rowmeta.ActionDTO;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.controller.BCFactory;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.CrudmaActionHolder.CrudmaAction;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.InterimResult;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.AnySourceBcDescription;
import org.cxbox.core.crudma.ext.CrudmaGatewayInvokeExtensionProvider;
import org.cxbox.core.crudma.state.BcState;
import org.cxbox.core.crudma.state.BcStateAware;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionType;
import org.cxbox.core.dto.rowmeta.ActionsDTO;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.core.dto.rowmeta.PostAction.BasePostActionField;
import org.cxbox.core.external.core.ParentDtoFirstLevelCache;
import org.cxbox.core.service.AnySourceResponseFactory;
import org.cxbox.core.service.AnySourceResponseService;
import org.cxbox.core.service.action.ActionAvailableChecker;
import org.cxbox.core.service.action.ActionDescriptionBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(100)
public class AnySourceBcStateCrudmaGatewayInvokeExtensionProvider implements CrudmaGatewayInvokeExtensionProvider {

	private final BcRegistry bcRegistry;

	private final BCFactory bcFactory;

	private final TransactionService transactionService;

	private final AnySourceResponseFactory respFactory;

	private final BcStateAware bcStateAware;

	private final ParentDtoFirstLevelCache parentDtoFirstLevelCache;

	@Override
	public <T> Invoker<T, RuntimeException> extendInvoker(CrudmaAction crudmaAction, Invoker<T, RuntimeException> invoker,
			boolean readOnly) {
		if (!(crudmaAction.getBc().getDescription() instanceof AnySourceBcDescription)) {
			return invoker;
		} else {
			return () -> {
				BusinessComponent bc = crudmaAction.getBc();
				CrudmaActionType action = crudmaAction.getActionType();
				if (Objects.equals(crudmaAction.getActionType(), CrudmaActionType.INVOKE) &&
						Objects.equals(ActionType.CANCEL_CREATE.getType(), crudmaAction.getName())
				) {
					bcStateAware.clear(bc);
					BcDescription description = bc.getDescription();
					if (description instanceof AnySourceBcDescription) {
						return (T) getResponseService(bc).onCancel(bc);
					}
					return (T) new ActionResultDTO().setAction(PostAction.postDelete());
				}
				restoreBcState(bc, action, readOnly);
				T invokeResult = invoker.invoke();
				afterInvoke(crudmaAction, readOnly, bc, action, invokeResult);
				return invokeResult;
			};
		}
	}

	private void afterInvoke(CrudmaAction crudmaAction, boolean readOnly, BusinessComponent bc, CrudmaActionType action,
			Object invokeResult) {

		if (action != null && !readOnly) {
			bcStateAware.clear(bc);
		}
		if (Objects.equals(crudmaAction.getActionType(), CrudmaActionType.CREATE) && readOnly) {
			InterimResult result = castToInterimResultOrElseThrow(invokeResult, crudmaAction.getActionType());
			result.setBc(getBcForState(
					bc.withId(result.getDto().getId()),
					result.getMeta().getPostActions()
			));
			BcState bcState = new BcState(null, false,
					Optional.ofNullable(crudmaAction.getOriginalActionType()).orElse(ActionType.CREATE.getType())
			);
			bcStateAware.set(result.getBc(), bcState);
			addActionCancel(bc, result.getMeta().getRow().getActions());
		}
		if (Objects.equals(crudmaAction.getActionType(), CrudmaActionType.PREVIEW) && readOnly) {
			InterimResult result = castToInterimResultOrElseThrow(invokeResult, crudmaAction.getActionType());
			boolean isRecordPersisted = bcStateAware.isPersisted(bc);
			bcStateAware.clear(bc);
			bcStateAware.set(
					result.getBc(),
					new BcState(
							result.getDto(),
							isRecordPersisted,
							Optional.ofNullable(result.getBc()).map(BusinessComponent::getParameters)
									.map(par -> par.getParameter("_action")).orElse((ActionType.CREATE.getType()))
					)
			);
			if (!bcStateAware.isPersisted(bc)) {
				addActionCancel(bc, result.getMeta().getRow().getActions());
			}
		}

		//TODO>>!bcStateAware.isPersisted(bc) и bcStateAware.getState(bc) != null && bcStateAware.getState(bc).getDto() != null равзве не одно и тоже?
		if (!bcStateAware.isPersisted(bc)) {
			if (CrudmaActionType.META.equals(crudmaAction.getActionType())) {
				MetaDTO meta = castToMetaDTOOrElseThrow(invokeResult, crudmaAction.getActionType());
				addActionCancel(bc, meta.getRow().getActions());
				meta.getRow().getFields().get(DataResponseDTO_.vstamp.getName()).setCurrentValue(-1L);
			} else if (CrudmaActionType.GET.equals(crudmaAction.getActionType())) {
				DataResponseDTO result = castToDataResponseDTOOrElseThrow(invokeResult, crudmaAction.getActionType());
				if (result != null) {
					result.setVstamp(-1L);
				}
			}
		} else {
			final CrudmaActionType actionType = crudmaAction.getActionType();
			if (CrudmaActionType.META.equals(actionType) && bcStateAware.getState(bc) != null
					&& bcStateAware.getState(bc).getDto() != null) {
				MetaDTO meta = castToMetaDTOOrElseThrow(invokeResult, crudmaAction.getActionType());
				final FieldDTO vstampField = meta.getRow().getFields().get(DataResponseDTO_.vstamp.getName());
				if (vstampField != null
						&& bcStateAware.getState(bc).getDto().getVstamp() < Long.parseLong(vstampField.getCurrentValue()
						.toString())) {
					vstampField.setCurrentValue(bcStateAware.getState(bc).getDto().getVstamp());
				}
			}
		}
	}

	private static InterimResult castToInterimResultOrElseThrow(Object invokeResult, CrudmaActionType actionType) {
		if (invokeResult instanceof InterimResult) {
			return (InterimResult) invokeResult;
		}
		throw new IllegalArgumentException(
				"invokeResult is expected to be InterimResult for CrudmaActionType = " + actionType);
	}

	private static DataResponseDTO castToDataResponseDTOOrElseThrow(Object invokeResult, CrudmaActionType actionType) {
		if (invokeResult instanceof DataResponseDTO) {
			return (DataResponseDTO) invokeResult;
		}
		throw new IllegalArgumentException(
				"invokeResult is expected to be InterimResult for DataResponseDTO = " + actionType);
	}

	private static MetaDTO castToMetaDTOOrElseThrow(Object invokeResult, CrudmaActionType actionType) {
		if (invokeResult instanceof MetaDTO) {
			return (MetaDTO) invokeResult;
		}
		throw new IllegalArgumentException(
				"invokeResult is expected to be InterimResult for CrudmaActionType = " + actionType);
	}

	private BusinessComponent getBcForState(final BusinessComponent bc, final List<PostAction> postActions) {
		for (final PostAction postAction : postActions) {
			if (DRILL_DOWN.equals(postAction.getAttribute(BasePostActionField.TYPE)) && INNER.getValue()
					.equals(postAction.getAttribute(BasePostActionField.DRILL_DOWN_TYPE))) {
				final String[] url = postAction.getAttribute(BasePostActionField.URL).split("/");
				if (Objects.equals(bc.getId(), url[url.length - 1])) {
					return new BusinessComponent(
							bc.getId(),
							bc.getParentId(),
							bcRegistry.getBcDescription(url[url.length - 2])
					);
				}
			}
		}
		return bc;
	}

	private void restoreBcState(final BusinessComponent currentBc, final CrudmaActionType action, boolean readOnly) {
		for (final BusinessComponent bc : Arrays.asList(getParentBcForRestore(currentBc), currentBc)) {
			if (bc == null) {
				continue;
			}
			final BcState state = bcStateAware.getState(bc);
			if (state == null) {
				continue;
			}
			if (!(bc.getDescription() instanceof AnySourceBcDescription)) {
				continue;
			}
			if (state.getPendingAction() != null) {
				QueryParameters originalParameters = bc.getParameters();
				originalParameters.setParameter("_action", state.getPendingAction());
				bc.setParameters(originalParameters);
			}
			final AnySourceResponseService<?, ?> responseService = getResponseService(bc);
			if (currentBc.equals(bc)) { //current bc
				if (!bcStateAware.isPersisted(bc)) {
					responseService.createEntity(bc);
				}

				if (state.getDto() != null && !EnumSet.of(UPDATE, PREVIEW, INVOKE).contains(action)) {
					responseService.updateEntity(bc, state.getDto());
				}
			} else { //parent
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
				parentDtoFirstLevelCache.getCache().put(bc.getName(), Optional.ofNullable(parentDto));
			}
		}
	}

	private BusinessComponent getParentBcForRestore(final BusinessComponent currentBc) {
		if (currentBc.getHierarchy() == null || currentBc.getHierarchy().getParent() == null) {
			return null;
		}
		return bcFactory.getBusinessComponent(
				currentBc.getHierarchy().getParent(),
				QueryParameters.onlyDatesQueryParameters(
						currentBc.getParameters()
				)
		);
	}

	private AnySourceResponseService<?, ?> getResponseService(BusinessComponent bc) {
		return respFactory.getService(bc.getDescription());
	}

	private void addActionCancel(BusinessComponent bc, final ActionsDTO actions) {
		boolean hasCancelAction = false;
		for (ActionDTO action : actions) {
			if (ActionType.DELETE.isTypeOf(action) || ActionType.CREATE.isTypeOf(action)) {
				action.setAvailable(false);
			}
			if (ActionType.CANCEL_CREATE.isTypeOf(action)) {
				action.setAvailable(true);
				hasCancelAction = true;
			}
		}

		if (hasCancelAction) {
			return;
		}

		actions.addMethod(
				0,
				new ActionDescriptionBuilder<>()
						.action(ActionType.CANCEL_CREATE)
						.available(ActionAvailableChecker.ALWAYS_TRUE)
						.withoutAutoSaveBefore()
						.build(null),
				bc
		);
	}

}
