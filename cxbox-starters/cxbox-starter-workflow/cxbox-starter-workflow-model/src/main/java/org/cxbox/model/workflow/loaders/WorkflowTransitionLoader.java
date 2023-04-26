/*-
 * #%L
 * IO Cxbox - Workflow Model
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

package org.cxbox.model.workflow.loaders;

import org.cxbox.model.core.loaders.AbstractObjectLoader;
import org.cxbox.model.workflow.entity.WorkflowTransition;
import org.springframework.stereotype.Component;


@Component
public class WorkflowTransitionLoader extends AbstractObjectLoader<WorkflowTransition> {

	@Override
	protected Class<? extends WorkflowTransition> getType() {
		return WorkflowTransition.class;
	}

	@Override
	public WorkflowTransition ensureLoaded(WorkflowTransition object) {
		return load(object);
	}

}
