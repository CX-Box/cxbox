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

package org.cxbox.core.crudma;

import java.util.List;
import org.cxbox.core.crudma.CrudmaActionHolder.CrudmaAction;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.CrudmaFactory;
import org.cxbox.core.crudma.CrudmaGateway;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.ExternalBcDescription;
import org.cxbox.core.crudma.ext.CrudmaGatewayInvokeExtensionProvider;
import org.cxbox.core.service.ExternalResponseFactory;
import org.cxbox.core.service.ExternalResponseService;
import org.cxbox.core.service.ResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class ExternalCrudmaGateway extends CrudmaGateway {

	@Autowired
	public ExternalResponseFactory respFactory;

	public ExternalCrudmaGateway(CrudmaFactory crudmaFactory, ResponseFactory respFactory, List<CrudmaGatewayInvokeExtensionProvider> extensionProviders) {
		super(crudmaFactory, respFactory, extensionProviders);
	}

	/**
	 * Determines whether to perform an action in a read-only transaction
	 *
	 * @param action is current request action
	 * @return should read only transaction begins
	 */
	@Override
	public boolean isReadOnly(CrudmaAction action) {
		boolean readOnly = super.isReadOnly(action);
		CrudmaActionType actionType = action.getActionType();
		BusinessComponent bc = action.getBc();
		BcDescription description = bc.getDescription();
		if (description instanceof ExternalBcDescription) {
			readOnly = actionType != null && actionType.isReadOnly();
			ExternalResponseService<?, ?> responseService = getResponseService(bc);
			if (CrudmaActionType.CREATE == actionType) {
				readOnly &= responseService.isDeferredCreationSupported(bc);
			}
		}
		return readOnly;
	}

	private ExternalResponseService<?, ?> getResponseService(BusinessComponent bc) {
		return respFactory.getService(bc.getDescription());
	}

}
