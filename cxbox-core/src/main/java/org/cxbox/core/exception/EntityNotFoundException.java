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

package org.cxbox.core.exception;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;


public class EntityNotFoundException extends BusinessException {

	public EntityNotFoundException() {
		super();
		addPopup(errorMessage("error.object_not_found_simple"));
	}

	public EntityNotFoundException(String type, long id) {
		super();
		addPopup(buildPopup(type, id));
	}

	public EntityNotFoundException(String type, String id) {
		super();
		addPopup(buildPopup(type, id));
	}

	private String buildPopup(String type, long id) {
		return errorMessage("error.object_not_found_full", type, id);
	}

	private String buildPopup(String type, String id) {
		return errorMessage("error.object_not_found_full", type, id);
	}

}
