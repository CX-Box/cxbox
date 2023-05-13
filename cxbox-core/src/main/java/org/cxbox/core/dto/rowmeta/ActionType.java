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

package org.cxbox.core.dto.rowmeta;

import static org.cxbox.api.util.i18n.LocalizationFormatter.uiMessage;
import static org.cxbox.core.service.action.ActionAvailableChecker.ALWAYS_TRUE;
import static org.cxbox.core.service.action.ActionAvailableChecker.NOT_NULL_ID;
import static org.cxbox.core.service.action.ActionAvailableChecker.NOT_NULL_PARENT_ID;

import org.cxbox.api.data.dto.rowmeta.ActionDTO;
import org.cxbox.core.service.action.ActionAvailableChecker;
import org.cxbox.core.service.action.CxboxActionIconSpecifier;

import java.util.Objects;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ActionType {

	CREATE("create", () -> uiMessage("action.create"), CxboxActionIconSpecifier.PLUS, NOT_NULL_PARENT_ID),
	CANCEL_CREATE("cancel-create", () -> uiMessage("action.cancel-create"), CxboxActionIconSpecifier.CLOSE, ALWAYS_TRUE),
	SAVE("save", () -> uiMessage("action.save"), CxboxActionIconSpecifier.SAVE, NOT_NULL_ID),
	COPY("copy", () -> uiMessage("action.copy"), CxboxActionIconSpecifier.COPY, NOT_NULL_ID),
	ASSOCIATE("associate", () -> uiMessage("action.add"), CxboxActionIconSpecifier.PLUS, NOT_NULL_PARENT_ID),
	DELETE("delete", () -> uiMessage("action.delete"), CxboxActionIconSpecifier.DELETE, NOT_NULL_ID);

	private final String type;

	private final Supplier<String> text;

	private final CxboxActionIconSpecifier icon;

	private final ActionAvailableChecker baseAvailableChecker;

	public boolean isTypeOf(ActionDTO action) {
		return action != null && Objects.equals(getType(), action.getType());
	}

}
