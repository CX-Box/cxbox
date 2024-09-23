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

import static org.cxbox.core.service.action.ActionAvailableChecker.ALWAYS_FALSE;
import static org.cxbox.core.service.action.ActionAvailableChecker.ALWAYS_TRUE;
import static java.util.Objects.nonNull;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.dto.rowmeta.ActionType;
import java.util.ArrayList;
import java.util.List;


public class ActionsBuilder<T extends DataResponseDTO> {

	private final List<ActionDescription<T>> actionDefinitions = new ArrayList<>();

	private final List<ActionGroupDescription<T>> actionGroupDefinitions = new ArrayList<>();

	private ActionDescriptionBuilder<T> actionDescriptionBuilder;

	ActionsBuilder() {

	}

	public ActionsBuilder<T> addAction(ActionDescription<T> actionDescription) {
		if (nonNull(actionDescription)) {
			actionDefinitions.add(actionDescription);
		}
		return this;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link org.cxbox.core.service.action.ActionsBuilder#action(java.util.function.UnaryOperator)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionDescriptionBuilder<T> newAction() {
		actionDescriptionBuilder = ActionDescription.<T>builder().withBuilder(this);
		actionDescriptionBuilder.available(ALWAYS_TRUE);
		return actionDescriptionBuilder;
	}

	public ActionsBuilder<T> action(UnaryOperator<ActionDescriptionBuilder<T>> descriptionBuilderConsumer) {
		var lambdaActionBuilder = ActionDescription.<T>builder().withBuilder(this);
		descriptionBuilderConsumer.apply(lambdaActionBuilder);
		var actionDescription = lambdaActionBuilder.build(null);
		addAction(actionDescription);
		return this;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link org.cxbox.core.service.action.ActionsBuilder#action(java.util.function.UnaryOperator)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionDescriptionBuilder<T> action(String type, String actionName) {
		actionDescriptionBuilder = newAction();
		actionDescriptionBuilder.action(type, actionName);
		return actionDescriptionBuilder;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link org.cxbox.core.service.action.ActionsBuilder#action(java.util.function.UnaryOperator)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionDescriptionBuilder<T> action(ActionType actionType) {
		actionDescriptionBuilder = newAction().action(actionType);
		return actionDescriptionBuilder;
	}

	public ActionsBuilder<T> create(UnaryOperator<ActionDescriptionBuilder<T>> descriptionBuilderConsumer) {
		var lambdaActionBuilder = create();
		descriptionBuilderConsumer.apply(lambdaActionBuilder);
		var actionDescription = lambdaActionBuilder.build(null);
		addAction(actionDescription);
		return this;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link org.cxbox.core.service.action.ActionsBuilder#create(java.util.function.UnaryOperator)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionDescriptionBuilder<T> create() {
		actionDescriptionBuilder = action(ActionType.CREATE)
				.scope(ActionScope.BC)
				.withoutAutoSaveBefore();
		return actionDescriptionBuilder;
	}

	public ActionsBuilder<T> save(UnaryOperator<ActionDescriptionBuilder<T>> descriptionBuilderConsumer) {
		var lambdaActionBuilder = save();
		descriptionBuilderConsumer.apply(lambdaActionBuilder);
		var actionDescription = lambdaActionBuilder.build(null);
		addAction(actionDescription);
		return this;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link org.cxbox.core.service.action.ActionsBuilder#save(java.util.function.UnaryOperator)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionDescriptionBuilder<T> save() {
		actionDescriptionBuilder = action(ActionType.SAVE);
		return actionDescriptionBuilder;
	}

	public ActionsBuilder<T> cancelCreate(UnaryOperator<ActionDescriptionBuilder<T>> descriptionBuilderConsumer) {
		var lambdaActionBuilder = cancelCreate();
		descriptionBuilderConsumer.apply(lambdaActionBuilder);
		var actionDescription = lambdaActionBuilder.build(null);
		addAction(actionDescription);
		return this;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link org.cxbox.core.service.action.ActionsBuilder#cancelCreate(java.util.function.UnaryOperator)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionDescriptionBuilder<T> cancelCreate() {
		// по-умолчанию недоступно, а решается в org.cxbox.core.crudma.CrudmaGateway
		actionDescriptionBuilder = action(ActionType.CANCEL_CREATE).available(ALWAYS_FALSE)
				.withoutAutoSaveBefore();
		return actionDescriptionBuilder;
	}

	public ActionsBuilder<T> associate(Consumer<ActionDescriptionBuilder<T>> descriptionBuilderConsumer) {
		var lambdaActionBuilder = associate();
		descriptionBuilderConsumer.accept(lambdaActionBuilder);
		var actionDescription = lambdaActionBuilder.build(null);
		addAction(actionDescription);
		return this;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link org.cxbox.core.service.action.ActionsBuilder#associate(java.util.function.Consumer)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionDescriptionBuilder<T> associate() {
		actionDescriptionBuilder = action(ActionType.ASSOCIATE)
				.scope(ActionScope.BC);
		return actionDescriptionBuilder;
	}

	public ActionsBuilder<T> delete(Consumer<ActionDescriptionBuilder<T>> descriptionBuilderConsumer) {
		var lambdaActionBuilder = delete();
		descriptionBuilderConsumer.accept(lambdaActionBuilder);
		var actionDescription = lambdaActionBuilder.build(null);
		addAction(actionDescription);
		return this;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link org.cxbox.core.service.action.ActionsBuilder#delete(java.util.function.Consumer)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionDescriptionBuilder<T> delete() {
		actionDescriptionBuilder = action(ActionType.DELETE)
				.withoutAutoSaveBefore();
		return actionDescriptionBuilder;
	}

	public ActionsBuilder<T> addGroup(String type, String groupName, int maxGroupVisualButtonsCount,
			Actions<T> groupActions) {
		actionGroupDefinitions.add(
				new ActionGroupDescription<>(type, groupName, maxGroupVisualButtonsCount, groupActions.actionDefinitions)
		);
		return this;
	}

	/**
	 * @deprecated Since 4.0.0-M7 use {@link ActionDescriptionBuilder#withIcon(ActionIconSpecifier, boolean)} instead
	 */
	@Deprecated(since = "4.0.0-M7")
	public ActionsBuilder<T> withIcon(ActionIconSpecifier icon, boolean showOnlyIcon) {
		this.actionGroupDefinitions.get(actionGroupDefinitions.size() - 1).setIconCode(icon);
		this.actionGroupDefinitions.get(actionGroupDefinitions.size() - 1).setShowOnlyIcon(showOnlyIcon);
		return this;
	}

	public ActionsBuilder<T> addAll(Actions<T> actions) {
		actionDefinitions.addAll(actions.actionDefinitions);
		actionGroupDefinitions.addAll(actions.actionGroupDefinitions);
		return this;
	}

	public ActionsBuilder<T> add(String key, ResponseServiceAction<T> responseServiceAction) {
		actionDefinitions.add(
				new ActionDescription<>(
						key,
						responseServiceAction.getButtonName(),
						responseServiceAction.getCustomParameters(),
						responseServiceAction::isAvailable,
						responseServiceAction::invoke,
						responseServiceAction::preActionSpecifier,
						responseServiceAction::preActionEventSpecifier,
						responseServiceAction::dataValidator,
						responseServiceAction.getIcon().getActionIconCode(),
						responseServiceAction.isIconWithText(),
						responseServiceAction.getScope(),
						responseServiceAction.isAutoSaveBefore()
				)
		);
		return this;
	}

	public Actions<T> build() {
		return new Actions<>(
				actionDefinitions,
				actionGroupDefinitions
		);
	}

}
