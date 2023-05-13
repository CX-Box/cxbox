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

package org.cxbox.engine.workflow.validation;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.model.workflow.entity.WorkflowTransitionValidation;
import org.cxbox.model.workflow.entity.WorkflowableTask;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UnsupportedValidator implements TransitionValidator<WorkflowableTask, WorkflowTransitionValidation> {

	@Override
	public LOV getType() {
		return null;
	}

	@Override
	public List<String> validate(final WorkflowableTask task, final WorkflowTransitionValidation transitionValidation) {
		throw new UnsupportedOperationException(String.format(
				"Проверка возможности перехода %s не реализована", transitionValidation.getValidCd().getKey()
		));
	}

}
