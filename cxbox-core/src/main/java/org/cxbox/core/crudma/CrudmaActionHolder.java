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

package org.cxbox.core.crudma;

import static org.cxbox.core.util.SpringBeanUtils.getBean;

import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.bc.BusinessComponent;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;


@Component
@RequestScope
public class CrudmaActionHolder {

	private CrudmaAction crudmaAction;

	public static CrudmaActionType getActionType() {
		return Optional.ofNullable(getCrudmaAction())
				.map(CrudmaAction::getActionType)
				.orElse(null);
	}

	public static CrudmaAction getCrudmaAction() {
		if (RequestContextHolder.getRequestAttributes() != null) {
			return Optional.ofNullable(getBean(CrudmaActionHolder.class))
					.map(CrudmaActionHolder::getAction)
					.orElse(null);
		}
		return null;
	}

	@Deprecated
	public static QueryParameters getQueryParameters() {
		return Optional.ofNullable(getCrudmaAction())
				.map(CrudmaAction::getBc)
				.map(BusinessComponent::getParameters)
				.orElse(QueryParameters.emptyQueryParameters());
	}

	public CrudmaAction getAction() {
		return crudmaAction;
	}

	public CrudmaActionHolder of(CrudmaActionType actionType) {
		crudmaAction = new SimpleCrudmaAction(actionType);
		return this;
	}

	public CrudmaActionHolder setDescription(String description) {
		Objects.requireNonNull(crudmaAction).setDescription(description);
		return this;
	}

	public CrudmaActionHolder setName(String name) {
		Objects.requireNonNull(crudmaAction).setName(name);
		return this;
	}

	public CrudmaActionHolder setBc(BusinessComponent bc) {
		Objects.requireNonNull(crudmaAction).setBc(bc);
		return this;
	}

	public CrudmaActionHolder setOriginalActionType(String originalActionType) {
		Objects.requireNonNull(crudmaAction).setOriginalActionType(originalActionType);
		return this;
	}

	public interface CrudmaAction {

		String toString();

		CrudmaActionType getActionType();

		String getDescription();

		String getName();

		org.cxbox.core.crudma.bc.BusinessComponent getBc();

		String getOriginalActionType();

		CrudmaActionHolder.CrudmaAction setDescription(String description);

		CrudmaActionHolder.CrudmaAction setName(String name);

		CrudmaActionHolder.CrudmaAction setBc(org.cxbox.core.crudma.bc.BusinessComponent bc);

		CrudmaActionHolder.CrudmaAction setOriginalActionType(String originalActionType);

	}
	
	@RequiredArgsConstructor
	@Accessors(chain = true)
	@ToString
	public static class SimpleCrudmaAction implements CrudmaAction {

		@Getter
		private final CrudmaActionType actionType;

		@Setter
		@Getter
		private String description;

		@Setter
		@Getter
		private String name;

		@Setter
		@Getter
		private BusinessComponent bc;

		/**
		 * If action has been initiated as custom action and transformed to CrudmaAction via `actionRole` parameter,
		 * this field is used to store an original action name.
		 */
		@Getter
		@Setter
		private String originalActionType;

	}

}
