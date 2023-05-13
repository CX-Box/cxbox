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

package org.cxbox.engine.workflow.function;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.dict.WorkflowDictionaries.WfPostFunction;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.engine.workflow.services.WorkflowDao;
import org.cxbox.model.workflow.entity.WorkflowPostFunction;
import org.cxbox.model.workflow.entity.WorkflowTransitionHistory;
import org.cxbox.model.workflow.entity.WorkflowableTask;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


@Service
public class PostFunctionAssignPrevious implements PostFunction<WorkflowableTask, WorkflowPostFunction> {

	@Lazy
	@Autowired
	private WorkflowDao workflowDao;

	@Override
	public LOV getType() {
		return WfPostFunction.ASSIGN_PREVIOUS;
	}

	@Override
	public List<PostAction> invoke(BcDescription bcDescription, WorkflowableTask task,
			WorkflowPostFunction postFunction) {
		WorkflowTransitionHistory lastTransitionHistoryStep = workflowDao.getLastTransitionHistory(task.getWorkflowTask());
		if (lastTransitionHistoryStep != null) {
			task.setAssignee(lastTransitionHistoryStep.getPreviousAssignee());
		}
		return Collections.emptyList();
	}

}
