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

package org.cxbox.core.crudma;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.AssociateDTO;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.PreviewResult;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.crudma.CrudmaActionHolder.CrudmaAction;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.crudma.ext.CrudmaGatewayInvokeExtensionProvider;
import org.cxbox.core.dto.BusinessError.Entity;
import org.cxbox.core.dto.MessageType;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.AssociateResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.core.exception.BusinessIntermediateException;
import org.cxbox.core.service.ResponseFactory;
import org.cxbox.core.service.ResponseService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrudmaGateway {

	private final CrudmaFactory crudmaFactory;

	private final ResponseFactory respFactory;

	private final List<CrudmaGatewayInvokeExtensionProvider> extensionProviders;

	public DataResponseDTO get(CrudmaAction crudmaAction) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		return invoke(crudmaAction, () -> getCrudmaService(bc).get(bc), readOnly);
	}

	public ResultPage<? extends DataResponseDTO> getAll(CrudmaAction crudmaAction) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		return invoke(crudmaAction, () -> getCrudmaService(bc).getAll(bc), readOnly);
	}

	public MetaDTO create(CrudmaAction crudmaAction) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		final InterimResult result = invoke(crudmaAction, () -> {
			final Crudma crudmaService = getCrudmaService(bc);
			final CreateResult<DataResponseDTO> createResult = crudmaService.create(bc);
			if (readOnly) {
				// мы откатываем транзакцию, помечаем DTO специальным флагом
				createResult.getRecord().setVstamp(-1L);
			}
			final MetaDTO metaNew = crudmaService.getMetaNew(bc, createResult);
			return new InterimResult(
					bc,
					createResult.getRecord(),
					metaNew
			);
		}, readOnly);
		return result.getMeta();
	}

	public MetaDTO preview(CrudmaAction crudmaAction, Map<String, Object> data) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		final InterimResult result = invoke(crudmaAction, () -> {
			final Crudma crudmaService = getCrudmaService(bc);
			final PreviewResult previewResult = crudmaService.preview(bc, data);
			if (readOnly) {
				// мы откатываем транзакцию, поэтому ставим старую версию
				previewResult.getResponseDto().setVstamp(previewResult.getRequestDto().getVstamp());
			}
			final MetaDTO metaNew = crudmaService.getOnFieldUpdateMeta(bc, previewResult.getResponseDto());
			return new InterimResult(bc, previewResult.getRequestDto(), metaNew);
		}, readOnly);
		if (result.getDto().getErrors() == null) {
			return result.getMeta();
		}
		throw new BusinessIntermediateException()
				.setObject(result.getMeta())
				.setEntity((Entity) result.getDto().getErrors());
	}

	public ActionResultDTO update(CrudmaAction crudmaAction, Map<String, Object> data) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		return invoke(crudmaAction, () -> getCrudmaService(bc).update(bc, data), readOnly);
	}

	public ActionResultDTO delete(CrudmaAction crudmaAction) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		if (bc.getId() == null) {
			return new ActionResultDTO().setAction(
					PostAction.showMessage(MessageType.WARNING, errorMessage("warn.no_record_to_delete"))
			);
		}
		// need for audit
		return invoke(crudmaAction, () -> getCrudmaService(bc).delete(bc), readOnly);
	}

	public ActionResultDTO invokeAction(CrudmaAction crudmaAction, Map<String, Object> data) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		String actionName = crudmaAction.getName();
		return invoke(crudmaAction, () -> getCrudmaService(bc).invokeAction(bc, actionName, data), readOnly);
	}

	public AssociateResultDTO associate(CrudmaAction crudmaAction, List<AssociateDTO> data) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		return invoke(crudmaAction, () -> getCrudmaService(bc).associate(bc, data), readOnly);
	}

	public MetaDTO getMeta(CrudmaAction crudmaAction) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		final MetaDTO meta = invoke(crudmaAction, () -> getCrudmaService(bc).getMeta(bc), readOnly);
		return meta;
	}

	public MetaDTO getMetaEmpty(CrudmaAction crudmaAction) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		return invoke(crudmaAction, () -> getCrudmaService(bc).getMetaEmpty(bc), readOnly);
	}

	public long count(CrudmaAction crudmaAction) {
		BusinessComponent bc = crudmaAction.getBc();
		boolean readOnly = isReadOnly(crudmaAction);
		return invoke(crudmaAction, () -> getCrudmaService(bc).count(bc), readOnly);
	}

	private <T> T invoke(CrudmaAction crudmaAction, Invoker<T, RuntimeException> invoker, boolean readOnly) {
		Invoker<T, RuntimeException> extendableInvoker = invoker;
		for (CrudmaGatewayInvokeExtensionProvider extensionProvider : extensionProviders) {
			extendableInvoker = extensionProvider.extendInvoker(crudmaAction, extendableInvoker, readOnly);
		}
		return extendableInvoker.invoke();
	}

	/**
	 * Determines whether to perform an action in a read-only transaction
	 *
	 * @param action is current request action
	 * @return should read only transaction begins
	 */
	public boolean isReadOnly(CrudmaAction action) {
		CrudmaActionType actionType = action.getActionType();
		BusinessComponent bc = action.getBc();
		BcDescription description = bc.getDescription();
		// todo: implement for extreme bc
		boolean readOnly = false;
		if (description instanceof InnerBcDescription) {
			readOnly = actionType != null && actionType.isReadOnly();
			ResponseService<?, ?> responseService = getResponseService(bc);
			if (CrudmaActionType.CREATE == actionType) {
				readOnly &= responseService.isDeferredCreationSupported(bc);
			}
		}
		return readOnly;
	}

	private ResponseService<?, ?> getResponseService(BusinessComponent bc) {
		return respFactory.getService(bc.getDescription());
	}

	private Crudma getCrudmaService(final BusinessComponent bc) {
		return crudmaFactory.get(bc.getDescription());
	}

}
