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

package org.cxbox.engine.workflow.preinvoke;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.engine.workflow.services.PreInvokeConditionChecker;
import org.cxbox.model.workflow.entity.WorkflowTransitionValidation;
import org.cxbox.model.workflow.entity.WorkflowableTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlwaysPreInvoke implements PreInvokeConditionChecker<WorkflowableTask> {

	@Override
	public LOV getType() {
		return null;
	}

	@Override
	public boolean check(WorkflowableTask task, WorkflowTransitionValidation transitionValidation) {
		return true;
	}

}
