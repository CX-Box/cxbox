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

package org.cxbox.core.crudma.impl.inner;

import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.AssociateDTO;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.PreviewResult;
import org.cxbox.api.exception.ServerException;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.crudma.impl.AbstractCrudmaService;
import org.cxbox.core.dto.rowmeta.*;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.service.ResponseFactory;
import org.cxbox.core.service.ResponseService;
import org.cxbox.core.service.action.ActionDescription;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.service.rowmeta.RowMetaType;
import org.cxbox.core.service.rowmeta.RowResponseService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

@Service
public class InnerCrudmaService extends AbstractCrudmaService {

	@Autowired
	private ResponseFactory respFactory;

	@Lazy
	@Autowired
	private RowResponseService rowMeta;

	@Override
	public CreateResult create(BusinessComponent bc) {
		ResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		availabilityCheck(responseService, ActionType.CREATE.getType(), bc);
		return responseService.createEntity(bc);
	}

	@Override
	public DataResponseDTO get(BusinessComponent bc) {
		ResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		return responseService.getOne(bc);
	}

	@Override
	public ResultPage<? extends DataResponseDTO> getAll(BusinessComponent bc) {
		ResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		return responseService.getList(bc);
	}

	@Override
	public PreviewResult preview(BusinessComponent bc, Map<String, Object> data) {
		final InnerBcDescription bcDescription = bc.getDescription();
		final ResponseService<?, ?> responseService = respFactory.getService(bcDescription);
		final DataResponseDTO requestDto = respFactory.getDTOFromMapIgnoreBusinessErrors(
				data, respFactory.getDTOFromService(bcDescription), bc
		);
		final DataResponseDTO responseDto = responseService.preview(bc, requestDto).getRecord();

		responseDto.setErrors(requestDto.getErrors());
		return new PreviewResult(requestDto, responseDto);
	}

	@Override
	public ActionResultDTO update(BusinessComponent bc, Map<String, Object> data) {
		final InnerBcDescription bcDescription = bc.getDescription();
		ResponseService<?, ?> responseService = respFactory.getService(bcDescription);
		availabilityCheck(responseService, ActionType.SAVE.getType(), bc);
		DataResponseDTO requestDTO = respFactory.getDTOFromMap(data, respFactory.getDTOFromService(bcDescription), bc);
		responseService.validate(bc, requestDTO);
		return responseService.updateEntity(bc, requestDTO);
	}

	@Override
	public ActionResultDTO delete(BusinessComponent bc) {
		ResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		availabilityCheck(responseService, ActionType.DELETE.getType(), bc);
		return responseService.deleteEntity(bc);
	}

	@Override
	public AssociateResultDTO associate(BusinessComponent bc, List<AssociateDTO> data) {
		ResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		availabilityCheck(responseService, ActionType.ASSOCIATE.getType(), bc);
		return responseService.associate(data, bc);
	}

	@Override
	public ActionResultDTO invokeAction(BusinessComponent bc, String actionName, Map<String, Object> data) {
		final InnerBcDescription bcDescription = bc.getDescription();
		ResponseService<?, ?> responseService = respFactory.getService(bcDescription);
		DataResponseDTO requestDTO = respFactory.getDTOFromMap(data, respFactory.getDTOFromService(bcDescription), bc);
		return responseService.invokeAction(bc, actionName, requestDTO);
	}

	@Override
	public MetaDTO getMetaNew(BusinessComponent bc, CreateResult createResult) {
		final InnerBcDescription bcDescription = bc.getDescription();
		ResponseService<?, ?> responseService = getResponseService(bcDescription);
		return rowMeta.getResponse(RowMetaType.META_NEW, createResult, bc, responseService);
	}

	@Override
	public MetaDTO getMeta(BusinessComponent bc) {
		final InnerBcDescription bcDescription = bc.getDescription();
		ResponseService<?, ?> service = respFactory.getService(bcDescription);
		try {
			return rowMeta.getResponse(RowMetaType.META, getDto(service, bc), bc, service);
		} catch (BusinessException e) {
			throw new BusinessException().addPopup(e.getMessage());
		} catch (Exception e) {
			throw new ServerException(e.getMessage(), e);
		}
	}

	@Override
	public MetaDTO getMetaEmpty(BusinessComponent bc) {
		final InnerBcDescription bcDescription = bc.getDescription();
		ResponseService<?, ?> service = respFactory.getService(bcDescription);
		try {
			return rowMeta.getResponse(RowMetaType.META_EMPTY, getDto(service, bc), bc, service);
		} catch (BusinessException e) {
			throw new BusinessException().addPopup(e.getMessage());
		} catch (Exception e) {
			throw new ServerException(e.getMessage(), e);
		}
	}

	@Override
	public MetaDTO getOnFieldUpdateMeta(BusinessComponent bc, DataResponseDTO dto) {
		final InnerBcDescription bcDescription = bc.getDescription();
		final ResponseService<?, ?> service = respFactory.getService(bcDescription);
		return rowMeta.getResponse(RowMetaType.ON_FIELD_UPDATE_META, dto, bc, service);
	}

	@Override
	public long count(BusinessComponent bc) {
		ResponseService<?, ?> responseService = getResponseService(bc.getDescription());
		return responseService.count(bc);
	}

	@Override
	public Actions getActions(BcDescription bcDescription) {
		if (bcDescription instanceof InnerBcDescription inner) {
			return respFactory.getService(inner).getActions();
		} else {
			return Actions.builder().build();
		}
	}

	private ResponseService<?, ?> getResponseService(InnerBcDescription innerBcDescription) {
		return respFactory.getService(innerBcDescription);
	}

	@SneakyThrows
	private DataResponseDTO getDto(ResponseService<?, ?> service, BusinessComponent bc) {
		if (bc.getId() != null && service.hasPersister()) {
			return service.getOne(bc);
		}
		Class<?> dto = respFactory.getDTOFromService(bc.getDescription());
		return (DataResponseDTO) dto.getConstructor().newInstance();
	}

	private void availabilityCheck(ResponseService<?, ?> service, String actionName, BusinessComponent bc) {
		ActionDescription<?> action = service.getActions().getAction(actionName);
		if (action == null || !action.isAvailable(bc)) {
			throw new BusinessException().addPopup(
					errorMessage("error.action_unavailable", actionName)
			);
		}
	}

}
