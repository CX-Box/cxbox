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

import static java.util.Objects.nonNull;

import java.util.Arrays;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.ActionDTO;
import org.cxbox.api.data.dto.rowmeta.PreActionDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionType;
import org.cxbox.core.dto.rowmeta.PreAction;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ActionDescription<T extends DataResponseDTO> {

	@Getter
	private final String key;

	@Getter
	private final String text;

	@Getter
	private final Map<String, String> customParameters;

	private final ActionAvailableChecker actionAvailableChecker;

	private final ActionInvoker<T> actionInvoker;

	private final PreActionSpecifier preActionSpecifier;

	private final PreActionEventSpecifier preActionEventSpecifier;

	private final DataValidator<T> dataValidator;

	@Getter
	private final String iconCode;

	@Getter
	private final boolean showOnlyIcon;

	@Getter
	private final ActionScope actionScope;

	@Getter
	private final boolean autoSaveBefore;

	public static <T extends DataResponseDTO> ActionDescriptionBuilder<T> builder() {
		return new ActionDescriptionBuilder<>();
	}

	public boolean isAvailable(BusinessComponent bc) {
		if (Arrays.stream(ActionType.values()).noneMatch(e -> e.getType().equals(this.key)) && ActionScope.RECORD.equals(this.actionScope)) {
			return ActionAvailableChecker.and(ActionAvailableChecker.NOT_NULL_ID, actionAvailableChecker).isAvailable(bc);
		}
		return actionAvailableChecker.isAvailable(bc);
	}

	public boolean isUpdateRequired() {
		return actionInvoker.isUpdateRequired();
	}

	public ActionResultDTO<T> invoke(BusinessComponent bc, T data) {
		return actionInvoker.invoke(bc, data);
	}

	public PreAction withPreAction(BusinessComponent bc) {
		return preActionSpecifier.withPreAction(bc);
	}

	public List<PreActionEvent> withPreActionEvents(BusinessComponent bc) {
		return preActionEventSpecifier.withPreActionEvents(bc);
	}

	public List<String> validate(BusinessComponent bc, DataResponseDTO requestDTO, T data) {
		return dataValidator.validate(bc, requestDTO, data);
	}

	public ActionDTO toDto(BusinessComponent bc) {
		PreAction preAction = withPreAction(bc);
		return ActionDTO.builder()
				.available(isAvailable(bc))
				.customParameters(this.getCustomParameters())
				.type(this.getKey())
				.text(this.getText())
				.icon(this.getIconCode())
				.showOnlyIcon(this.isShowOnlyIcon())
				.preActionDTO(nonNull(preAction) ? PreActionDTO.builder()
						.type(preAction.getType())
						.message(preAction.getMessage(this.getText()))
						.customParameter(preAction.getCustomParameters())
						.build() : null)
				.scope(this.getActionScope().toString().toLowerCase())
				.autoSaveBefore(this.isAutoSaveBefore())
				.build();
	}

}
