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

package org.cxbox.core.service.action;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.PreAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public abstract class ResponseServiceAction<T extends DataResponseDTO> {

	public abstract String getButtonName();

	public Map<String, String> getCustomParameters() {
		return Collections.emptyMap();
	}

	public abstract boolean isAvailable(BusinessComponent bc);

	public abstract ActionResultDTO<T> invoke(BusinessComponent bc, T data);

	public PreAction preActionSpecifier(BusinessComponent bc) {
		return null;
	}

	public List<PreActionEvent> preActionEventSpecifier(BusinessComponent bc) {
		return Collections.emptyList();
	}

	public List<String> dataValidator(BusinessComponent bc, DataResponseDTO data, T entityDto) {
		return Collections.emptyList();
	}

	public ActionIconSpecifier getIcon() {
		return CxboxActionIconSpecifier.WITHOUT_ICON;
	}

	public ActionScope getScope() {
		return ActionScope.RECORD;
	}

	public boolean isAutoSaveBefore() {
		return true;
	}

	public boolean isIconWithText() {
		return false;
	}


}
