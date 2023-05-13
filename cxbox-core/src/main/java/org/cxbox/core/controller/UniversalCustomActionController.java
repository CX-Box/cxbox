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

import static org.cxbox.core.config.properties.APIProperties.CXBOX_API_PATH_SPEL;

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
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = CXBOX_API_PATH_SPEL + "/custom-action/**")
public class UniversalCustomActionController {

	@Autowired
	private CrudmaGateway crudmaGateway;

	@Autowired
	private BCFactory bcFactory;

	@Autowired
	private CrudmaActionHolder crudmaActionHolder;

	@RequestMapping(method = RequestMethod.POST)
	public ResponseDTO invoke(HttpServletRequest request,
			QueryParameters queryParameters,
			@RequestBody Map<String, Map<String, Object>> requestBody) {
		if (requestBody == null || requestBody.get("data") == null) {
			throw new ClientException("Request with wrong request body. Expected: {\"data\":{}}");
		}
		final BusinessComponent bc = bcFactory.getBusinessComponent(request, queryParameters);
		final String action = queryParameters.getParameter("_action");
		CrudmaAction crudmaAction = crudmaActionHolder.of(CrudmaActionType.INVOKE)
				.setBc(bc).setName(action).setDescription(
						String.format(
								"Выполнение действия %s.%s, id: %s, parentId: %s",
								bc.getDescription().getName(),
								action,
								bc.getId(),
								bc.getParentId()
						)
				).getAction();
		return ResponseBuilder.build(crudmaGateway.invokeAction(crudmaAction, requestBody.get("data")));
	}

}
