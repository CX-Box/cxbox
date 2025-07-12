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

package org.cxbox.core.crudma.impl.inner;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.AssociateDTO;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.DataResponseDTO.ChangedNowParam;
import org.cxbox.api.data.dto.rowmeta.PreviewResult;
import org.cxbox.api.exception.ServerException;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.AnySourceBcDescription;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.impl.AbstractCrudmaService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionType;
import org.cxbox.core.dto.rowmeta.AssociateResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.dao.AnySourceBaseDAO;
import org.cxbox.core.service.AnySourceResponseFactory;
import org.cxbox.core.service.AnySourceResponseService;
import org.cxbox.core.service.ChangedNowValidationService;
import org.cxbox.core.service.action.ActionDescription;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.service.rowmeta.AnySourceRowResponseService;
import org.cxbox.core.service.rowmeta.RowMetaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class AnySourceCrudmaService extends AbstractCrudmaService {

	@Autowired
	private AnySourceResponseFactory respFactory;

	@Lazy
	@Autowired
	private AnySourceRowResponseService rowMeta;

	@Autowired
	private ChangedNowValidationService changedNowValidationService;

	@Override
	public CreateResult create(BusinessComponent bc) {
		AnySourceResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		availabilityCheck(responseService, ActionType.CREATE.getType(), bc);
		return responseService.createEntity(bc);
	}

	@Override
	public DataResponseDTO get(BusinessComponent bc) {
		AnySourceResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		return responseService.getOne(bc);
	}

	@Override
	public ResultPage<? extends DataResponseDTO> getAll(BusinessComponent bc) {
		AnySourceResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		return responseService.getList(bc);
	}

	@Override
	public PreviewResult preview(BusinessComponent bc, Map<String, Object> data) {
		final AnySourceBcDescription bcDescription = bc.getDescription();
		final AnySourceResponseService<?, ?> responseService = respFactory.getService(bcDescription);
		final DataResponseDTO requestDto = respFactory.getDTOFromMapIgnoreBusinessErrors(
				data, respFactory.getDTOFromService(bcDescription), bc
		);
		final DataResponseDTO responseDto = responseService.preview(bc, requestDto).getRecord();
		if (changedNowValidationService.isChangedNowData(requestDto)) {
			HashMap<String, Object> changedNowMap = requestDto.getChangedNow_();
			DataResponseDTO changedNowDTO = respFactory.getDTOFromMapIgnoreBusinessErrors(
					changedNowMap, respFactory.getDTOFromService(bc.getDescription()), bc);
			changedNowValidationService.validateChangedNowFields(changedNowMap,changedNowDTO,requestDto);
			ChangedNowParam changedNowParam = changedNowValidationService.buildCnangedNowParam(new HashSet<>(changedNowMap.keySet()),changedNowDTO);
			responseDto.setChangedNowParam(changedNowParam);
		}
		responseDto.setErrors(requestDto.getErrors());
		return new PreviewResult(requestDto, responseDto);
	}

	@Override
	public ActionResultDTO update(BusinessComponent bc, Map<String, Object> data) {
		final AnySourceBcDescription bcDescription = bc.getDescription();
		AnySourceResponseService responseService = respFactory.getService(bcDescription);
		availabilityCheck(responseService, ActionType.SAVE.getType(), bc);
		DataResponseDTO requestDTO = respFactory.getDTOFromMap(data, respFactory.getDTOFromService(bcDescription), bc);
		responseService.validate(bc, requestDTO);
		final AnySourceBaseDAO dao = responseService.getBaseDao();
		final ActionResultDTO actionResultDTO = responseService.updateEntity(bc, requestDTO);
		dao.flush(bc);
		actionResultDTO.transformData(r -> responseService.entityToDto(bc, dao.getById(bc)));
		return actionResultDTO;
	}

	@Override
	public ActionResultDTO delete(BusinessComponent bc) {
		AnySourceResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		availabilityCheck(responseService, ActionType.DELETE.getType(), bc);
		return responseService.deleteEntity(bc);
	}

	@Override
	public AssociateResultDTO associate(BusinessComponent bc, List<AssociateDTO> data) {
		AnySourceResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		availabilityCheck(responseService, ActionType.ASSOCIATE.getType(), bc);
		return responseService.associate(data, bc);
	}

	@Override
	public ActionResultDTO invokeAction(BusinessComponent bc, String actionName, Map<String, Object> data) {
		final AnySourceBcDescription bcDescription = bc.getDescription();
		AnySourceResponseService<?, ?> responseService = respFactory.getService(bcDescription);
		DataResponseDTO requestDTO = respFactory.getDTOFromMap(data, respFactory.getDTOFromService(bcDescription), bc);
		return responseService.invokeAction(bc, actionName, requestDTO);
	}

	@Override
	public MetaDTO getMetaNew(BusinessComponent bc, CreateResult createResult) {
		final AnySourceBcDescription bcDescription = bc.getDescription();
		AnySourceResponseService<?, ?> responseService = getResponseService(bcDescription);
		return rowMeta.getAnySourceResponse(RowMetaType.META_NEW, createResult, bc, responseService);
	}

	@Override
	public MetaDTO getMeta(BusinessComponent bc) {
		final AnySourceBcDescription bcDescription = bc.getDescription();
		AnySourceResponseService<?, ?> service = respFactory.getService(bcDescription);
		try {
			return rowMeta.getAnySourceResponse(RowMetaType.META, getDto(service, bc), bc, service);
		} catch (BusinessException e) {
			throw new BusinessException().addPopup(e.getMessage());
		} catch (Exception e) {
			throw new ServerException(e.getMessage(), e);
		}
	}

	@Override
	public MetaDTO getMetaEmpty(BusinessComponent bc) {
		final AnySourceBcDescription bcDescription = bc.getDescription();
		AnySourceResponseService<?, ?> service = respFactory.getService(bcDescription);
		try {
			return rowMeta.getAnySourceResponse(RowMetaType.META_EMPTY, getDto(service, bc), bc, service);
		} catch (BusinessException e) {
			throw new BusinessException().addPopup(e.getMessage());
		} catch (Exception e) {
			throw new ServerException(e.getMessage(), e);
		}
	}

	@Override
	public MetaDTO getOnFieldUpdateMeta(BusinessComponent bc, DataResponseDTO dto) {
		final AnySourceBcDescription bcDescription = bc.getDescription();
		final AnySourceResponseService<?, ?> service = respFactory.getService(bcDescription);
		return rowMeta.getAnySourceResponse(RowMetaType.ON_FIELD_UPDATE_META, dto, bc, service);
	}

	@Override
	public long count(BusinessComponent bc) {
		AnySourceResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		return responseService.count(bc);
	}

	private AnySourceResponseService<?, ?> getResponseService(AnySourceBcDescription anySourceBcDescription) {
		return respFactory.getService(anySourceBcDescription);
	}

	@Override
	public Actions getActions(BcDescription bcDescription) {
		if (bcDescription instanceof AnySourceBcDescription any) {
			return respFactory.getService(any).getActions();
		} else {
			return Actions.builder().build();
		}
	}

	@SneakyThrows
	private DataResponseDTO getDto(AnySourceResponseService<?, ?> service, BusinessComponent bc) {
		if (bc.getId() != null && service.hasPersister()) {
			return service.getOne(bc);
		}
		Class<?> dto = respFactory.getDTOFromService(bc.getDescription());
		return (DataResponseDTO) dto.getConstructor().newInstance();
	}

	private void availabilityCheck(AnySourceResponseService<?, ?> service, String actionName, BusinessComponent bc) {
		ActionDescription<?> action = service.getActions().getAction(actionName);
		if (action == null || !action.isAvailable(bc)) {
			throw new BusinessException().addPopup(
					errorMessage("error.action_unavailable", actionName)
			);
		}
	}

}
