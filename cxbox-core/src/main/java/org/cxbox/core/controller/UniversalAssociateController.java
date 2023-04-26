/*-
 * #%L
 * IO Cxbox - Core
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

package org.cxbox.core.controller;

import static org.cxbox.core.config.properties.APIProperties.CXBOX_API_PATH_SPEL;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.cxbox.api.util.i18n.InfoMessageSource.infoMessage;

import org.cxbox.api.data.dto.AssociateDTO;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.CrudmaActionHolder;
import org.cxbox.core.crudma.CrudmaActionHolder.CrudmaAction;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.CrudmaGateway;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.ResponseDTO;
import org.cxbox.core.exception.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cxbox.core.util.ResponseBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = CXBOX_API_PATH_SPEL + "/associate/**")
public class UniversalAssociateController {

	@Autowired
	@Qualifier("cxboxObjectMapper")
	private ObjectMapper objectMapper;

	@Autowired
	private CrudmaGateway crudmaGateway;

	@Autowired
	private BCFactory bcFactory;

	@Autowired
	private CrudmaActionHolder crudmaActionHolder;

	@RequestMapping(method = POST)
	public ResponseDTO associate(HttpServletRequest request,
			QueryParameters queryParameters,
			@RequestBody List<Object> data) {
		BusinessComponent bc = bcFactory.getBusinessComponent(request, queryParameters);
		if (data == null) {
			throw new ClientException("request must contain body");
		}
		final String action = queryParameters.getParameter("_action");
		CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.ASSOCIATE)
				.setBc(bc).setOriginalActionType(action).setDescription(
						infoMessage(
								"info.associate_request",
								bc.getDescription().getName(),
								bc.getId(),
								bc.getParentId()
						)
				).getAction();
		return ResponseBuilder.build(crudmaGateway.associate(crudmaAction, convertData(data)));
	}

	private List<AssociateDTO> convertData(List<Object> data) {
		List<AssociateDTO> result = new ArrayList<>();
		data.forEach(d -> result.add(objectMapper.convertValue(d, AssociateDTO.class)));
		return result;
	}

}
