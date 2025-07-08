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

import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.CrudmaActionHolder;
import org.cxbox.core.crudma.CrudmaActionHolder.CrudmaAction;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.CrudmaGateway;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.ResponseDTO;
import org.cxbox.core.exception.ClientException;
import org.cxbox.core.util.ResponseBuilder;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(CXBOX_API_PATH_SPEL)
public class UniversalRowMetaController {

	private final CrudmaGateway crudmaGateway;

	private final BCFactory bcFactory;

	private final CrudmaActionHolder crudmaActionHolder;

	@SuppressWarnings("java:S3752")
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "row-meta-new/**")
	public ResponseDTO rowMetaNew(
			HttpServletRequest request,
			QueryParameters queryParameters) {
		final String action = queryParameters.getParameter("_action");
		BusinessComponent bc = bcFactory.getBusinessComponent(request, queryParameters);
		CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.CREATE)
				.setBc(bc).setOriginalActionType(action).setDescription(
						infoMessage(
								"info.record_create_request",
								bc.getDescription().getName(),
								bc.getParentId()
						)
				).getAction();
		return ResponseBuilder.build(crudmaGateway.create(crudmaAction));
	}

	@RequestMapping(method = RequestMethod.GET, value = "row-meta-empty/**")
	public ResponseDTO rowMetaEmpty(HttpServletRequest request, QueryParameters queryParameters) {
		BusinessComponent bc = bcFactory.getBusinessComponent(request, queryParameters);
		CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.META)
				.setBc(bc).setDescription(
						infoMessage(
								"info.row_meta_empty_request",
								bc.getDescription().getName(),
								bc.getParentId()
						)
				).getAction();
		return ResponseBuilder.build(crudmaGateway.getMetaEmpty(crudmaAction));
	}

	@RequestMapping(method = RequestMethod.GET, value = "row-meta/**")
	public ResponseDTO rowMeta(HttpServletRequest request, QueryParameters queryParameters) {
		BusinessComponent bc = bcFactory.getBusinessComponent(request, queryParameters);
		CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.META)
				.setBc(bc).setDescription(
						infoMessage(
								"info.row_meta_request",
								bc.getDescription().getName(),
								bc.getId(),
								bc.getParentId()
						)
				).getAction();
		return ResponseBuilder.build(crudmaGateway.getMeta(crudmaAction));
	}

	@RequestMapping(method = RequestMethod.POST, value = "row-meta/**")
	public ResponseDTO onFieldUpdateMeta(
			HttpServletRequest request,
			QueryParameters queryParameters,
			@RequestBody Map<String, Object> requestBody) {
		if (requestBody == null || requestBody.get(DATA) == null || !(requestBody.get(DATA) instanceof Map)) {
			throw new ClientException("Request with wrong request body. Expected: {\"data\":{}}");
		}
		BusinessComponent bc = bcFactory.getBusinessComponent(request, queryParameters);
		CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.PREVIEW)
				.setBc(bc).setDescription(
						infoMessage(
								"info.record_preview_request",
								bc.getDescription().getName(),
								bc.getId(),
								bc.getParentId()
						)
				).getAction();
		return ResponseBuilder.build(crudmaGateway.preview(crudmaAction, requestBody));
	}

}
