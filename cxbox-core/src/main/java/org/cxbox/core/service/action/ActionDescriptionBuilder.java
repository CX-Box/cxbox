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
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cxbox.core.service.action.ActionAvailableChecker.and;
import static org.cxbox.core.service.action.CxboxActionIconSpecifier.WITHOUT_ICON;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionType;
import org.cxbox.core.dto.rowmeta.PreAction;

@NoArgsConstructor
public class ActionDescriptionBuilder<T extends DataResponseDTO> {

	private static final String ACTION_ROLE = "actionRole";

	private static final String FILE_UPLOAD_ROLE = "file-upload";

	private String key;

	private String text;

	private ActionAvailableChecker baseActionAvailableChecker;

	private ActionAvailableChecker actionAvailableChecker;

	private ActionInvoker<T> actionInvoker;

	private PreActionSpecifier preActionSpecifier;

	private PreActionEventSpecifier preActionEventSpecifier;

	private DataValidator<T> dataValidator;

	private ActionsBuilder<T> actionsBuilder;

	private ActionIconSpecifier iconCode = WITHOUT_ICON;

	private boolean showOnlyIcon = false;

	private ActionScope actionScope = ActionScope.RECORD;

	private Map<String, String> customParameter;

	private Boolean updateRequired;

	private boolean autoSaveBefore = true;

	private boolean withoutTransaction;

	private static <T extends DataResponseDTO> ActionInvoker<T> withoutUpdate(ActionInvoker<T> wrapped) {

		return new ActionInvoker<T>() {
			@Override
			public ActionResultDTO<T> invoke(BusinessComponent bc, T data) {
				return wrapped.invoke(bc, data);
			}

			@Override
			public boolean isUpdateRequired() {
				return false;
			}

		};

	}

	private static <T extends DataResponseDTO> ActionInvoker<T> withUpdate(ActionInvoker<T> wrapped) {

		return new ActionInvoker<T>() {
			@Override
			public ActionResultDTO<T> invoke(BusinessComponent bc, T data) {
				return wrapped.invoke(bc, data);
			}

			@Override
			public boolean isUpdateRequired() {
				return true;
			}

		};

	}

	public ActionDescriptionBuilder<T> action(String key, String text) {
		this.key = key;
		this.text = text;
		return this;
	}

	public ActionDescriptionBuilder<T> action(ActionType actionType) {
		if (nonNull(actionType)) {
			this.key = actionType.getType();
			this.text = actionType.getText().get();
			this.iconCode = actionType.getIcon();
			this.showOnlyIcon = true;
			this.baseActionAvailableChecker = actionType.getBaseAvailableChecker();
		}
		return this;
	}

	public ActionDescriptionBuilder<T> available(ActionAvailableChecker actionAvailableChecker) {
		this.actionAvailableChecker = actionAvailableChecker;
		return this;
	}

	public ActionDescriptionBuilder<T> invoker(ActionInvoker<T> actionInvoker) {
		this.actionInvoker = actionInvoker;
		return this;
	}

	public @NonNull ActionDescriptionBuilder<T> massInvoker(@NonNull MassActionInvoker<T> massActionInvoker) {
		this.scope(ActionScope.MASS);
		this.actionInvoker = massActionInvoker.toInvoker();
		return this;
	}

	public ActionDescriptionBuilder<T> text(String text) {
		this.text = text;
		return this;
	}

	public ActionDescriptionBuilder<T> withPreAction(PreActionSpecifier preActionSpecifier) {
		this.preActionSpecifier = preActionSpecifier;
		return this;
	}

	public ActionDescriptionBuilder<T> withPreAction(PreAction preAction) {
		this.preActionSpecifier = bc -> preAction;
		return this;
	}

	public ActionDescriptionBuilder<T> withIcon(ActionIconSpecifier icon, boolean showOnlyIcon) {
		this.iconCode = icon;
		this.showOnlyIcon = showOnlyIcon;
		return this;
	}

	public ActionDescriptionBuilder<T> scope(ActionScope actionScope) {
		this.actionScope = actionScope;
		return this;
	}

	public ActionDescriptionBuilder<T> withCustomParameter(Map<String, String> parametersTuple) {
		this.customParameter = parametersTuple;
		return this;
	}

	public ActionDescriptionBuilder<T> withAutoSaveBefore() {
		this.autoSaveBefore = true;
		return this;
	}

	public ActionDescriptionBuilder<T> withoutAutoSaveBefore() {
		this.autoSaveBefore = false;
		return this;
	}

	/**
	 * The action runs without a transaction through the {@code custom-action-without-tx} endpoint,
	 * the usual {@code custom-action} refuses it; only a mass action so far.
	 * The checks, pre-invoke events, auto save and record loading run in a transaction that is rolled back:
	 * the invoker gets the changed record unsaved and saves what it needs in its own transactions.
	 * Turns the record lock off ({@link #updateRequired(Boolean)} after this method turns it on again).
	 */
	public ActionDescriptionBuilder<T> withoutTransaction() {
		this.withoutTransaction = true;
		this.updateRequired = false;
		return this;
	}

	public ActionDescriptionBuilder<T> withoutIcon() {
		this.iconCode = WITHOUT_ICON;
		this.showOnlyIcon = false;
		return this;
	}

	public ActionDescriptionBuilder<T> withPreActionEvents(PreActionEventSpecifier preActionEventSpecifier) {
		this.preActionEventSpecifier = preActionEventSpecifier;
		return this;
	}

	public ActionDescriptionBuilder<T> withPreActionEvents(PreActionEvent... preActionEvents) {
		this.preActionEventSpecifier = bc -> nonNull(preActionEvents) ? Arrays.asList(preActionEvents) : null;
		return this;
	}

	public ActionDescriptionBuilder<T> validator(DataValidator<T> dataValidator) {
		this.dataValidator = dataValidator;
		return this;
	}

	ActionDescriptionBuilder<T> withBuilder(ActionsBuilder<T> actionsBuilder) {
		this.actionsBuilder = actionsBuilder;
		return this;
	}

	public ActionDescriptionBuilder<T> updateRequired(Boolean updateRequired) {
		this.updateRequired = updateRequired;
		return this;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link org.cxbox.core.service.action.ActionsBuilder#action(java.util.function.UnaryOperator)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionsBuilder<T> add(Boolean updateRequired) {
		ActionDescription<T> actionDescription = this.build(updateRequired);
		this.actionsBuilder.addAction(actionDescription);
		return this.actionsBuilder;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link org.cxbox.core.service.action.ActionsBuilder#action(java.util.function.UnaryOperator)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionsBuilder<T> add() {
		return add(null);
	}

	public ActionDescription<T> build(Boolean updateRequired) {
		if (withoutTransaction && isStandardActionType(key)) {
			throw new IllegalStateException("Standard action " + key + " cannot be marked to run without transaction");
		}
		if (withoutTransaction && !ActionScope.MASS.equals(actionScope)) {
			throw new IllegalStateException(
					"Action " + key + " cannot be marked to run without transaction: only mass actions are supported"
			);
		}
		String actionRole = customParameter == null ? null : customParameter.get(ACTION_ROLE);
		if (withoutTransaction && (isStandardActionType(actionRole) || FILE_UPLOAD_ROLE.equals(actionRole))) {
			throw new IllegalStateException(
					"Action " + key + " with " + ACTION_ROLE + " " + actionRole + " cannot be marked to run without transaction"
			);
		}
		ActionInvoker<T> invoker;
		if (actionInvoker == null) {
			invoker = ActionInvoker.UNSUPPORTED_OPERATION;
		} else if (updateRequired == null && !withoutTransaction) {
			invoker = actionInvoker;
		} else {
			invoker = Boolean.TRUE.equals(updateRequired) ? withUpdate(actionInvoker) : withoutUpdate(actionInvoker);
		}
		ActionDescription<T> actionDescription = new ActionDescription<>(
				key,
				text,
				customParameter,
				and(
						defaultIfNull(baseActionAvailableChecker, ActionAvailableChecker.ALWAYS_TRUE),
						defaultIfNull(actionAvailableChecker, ActionAvailableChecker.ALWAYS_TRUE)
				),
				invoker,
				defaultIfNull(preActionSpecifier, PreActionSpecifierType.WITHOUT_PREACTION),
				defaultIfNull(preActionEventSpecifier, bc -> null),
				defaultIfNull(dataValidator, (bc, data, entityDto) -> Collections.emptyList()),
				iconCode.getActionIconCode(),
				showOnlyIcon,
				actionScope,
				autoSaveBefore
		);
		actionDescription.setWithoutTransaction(withoutTransaction);
		return actionDescription;
	}

	public ActionDescription<T> build() {
		return build(updateRequired);
	}

	private static boolean isStandardActionType(String type) {
		return Arrays.stream(ActionType.values()).anyMatch(actionType -> actionType.getType().equals(type));
	}

}
