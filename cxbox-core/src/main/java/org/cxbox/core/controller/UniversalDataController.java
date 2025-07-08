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

package org.cxbox.core.controller;

import static org.cxbox.api.util.i18n.InfoMessageSource.infoMessage;
import static org.cxbox.core.config.properties.APIProperties.CXBOX_API_PATH_SPEL;
import static org.cxbox.core.controller.param.RequestParameters.DATA;

import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.CrudmaActionHolder;
import org.cxbox.core.crudma.CrudmaActionHolder.CrudmaAction;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.CrudmaGateway;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.ResponseDTO;
import org.cxbox.core.exception.ClientException;
import org.cxbox.core.util.ResponseBuilder;
import java.util.Collections;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CXBOX_API_PATH_SPEL)
public class UniversalDataController {

	@Autowired
	private CrudmaGateway crudmaGateway;

	@Autowired
	private BCFactory bcFactory;

	@Autowired
	private CrudmaActionHolder crudmaActionHolder;

	@SuppressWarnings("java:S3752")
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"data/**"})
	public ResponseDTO find(
			HttpServletRequest request,
			QueryParameters queryParameters) {
		BusinessComponent bc = bcFactory.getBusinessComponent(request, queryParameters);
		if (bc.getId() != null) {
			CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.GET)
					.setBc(bc).getAction();
			crudmaAction.setDescription(
					String.format(
							"Получение записи %s, id: %s, parentId: %s",
							bc.getDescription().getName(),
							bc.getId(),
							bc.getParentId()
					)
			);
			DataResponseDTO data = crudmaGateway.get(crudmaAction);
			return ResponseBuilder.build(data == null ? Collections.emptyList() : Collections.singletonList(data));
		} else {
			CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.FIND)
					.setBc(bc).getAction();
			crudmaAction.setDescription(
					infoMessage("info.get_list_request", bc.getDescription().getName(), bc.getParentId())
			);
			ResultPage<? extends DataResponseDTO> data = crudmaGateway.getAll(crudmaAction);
			return ResponseBuilder.build(data.getResult(), data.isHasNext());
		}
	}

	@RequestMapping(method = RequestMethod.PUT, value = {"data/**"})
	public ResponseDTO update(HttpServletRequest request,
			QueryParameters queryParameters,
			@RequestBody Map<String, Object> requestBody) {
		if (requestBody == null || requestBody.get(DATA) == null || !(requestBody.get(DATA) instanceof Map)) {
			throw new ClientException("Request with wrong request body. Expected: {\"data\":{}}");
		} else {
			requestBody = (Map) requestBody.get(DATA);
		}
		final String action = queryParameters.getParameter("_action");
		BusinessComponent bc = bcFactory.getBusinessComponent(request, queryParameters);
		CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.UPDATE)
				.setBc(bc).setOriginalActionType(action).setDescription(
						String.format(
								"Изменение записи %s, id: %s, parentId: %s",
								bc.getDescription().getName(),
								bc.getId(),
								bc.getParentId()
						)
				).getAction();
		return ResponseBuilder.build(crudmaGateway.update(crudmaAction, requestBody));
	}

	@RequestMapping(method = RequestMethod.DELETE, value = {"data/**"})
	public ResponseDTO delete(HttpServletRequest request,
			QueryParameters queryParameters
	) {
		BusinessComponent bc = bcFactory.getBusinessComponent(request, queryParameters);
		final String action = queryParameters.getParameter("_action");
		CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.DELETE)
				.setBc(bc).setOriginalActionType(action).setDescription(
						String.format(
								"Удаление записи %s, id: %s, parentId: %s",
								bc.getDescription().getName(),
								bc.getId(),
								bc.getParentId()
						)
				).getAction();
		return ResponseBuilder.build(crudmaGateway.delete(crudmaAction));
	}

	@SuppressWarnings("java:S3752")
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"count/**"})
	public ResponseDTO count(
			HttpServletRequest request,
			QueryParameters queryParameters
	) {
		BusinessComponent bc = bcFactory.getBusinessComponent(request, queryParameters);
		CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.COUNT)
				.setBc(bc).setDescription(
						String.format(
								"Получение количества записей %s, parentId: %s",
								bc.getDescription().getName(),
								bc.getParentId()
						)
				).getAction();
		return ResponseBuilder.build(crudmaGateway.count(crudmaAction));
	}

}
