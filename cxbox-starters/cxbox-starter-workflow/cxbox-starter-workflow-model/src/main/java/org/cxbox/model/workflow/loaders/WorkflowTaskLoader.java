
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

package org.cxbox.model.workflow.loaders;

import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.core.loaders.AbstractObjectLoader;
import org.cxbox.model.workflow.entity.WorkflowStep;
import org.cxbox.model.workflow.entity.WorkflowStep_;
import org.cxbox.model.workflow.entity.WorkflowTask;
import org.cxbox.model.workflow.entity.WorkflowVersion_;
import org.cxbox.model.workflow.entity.Workflow_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class WorkflowTaskLoader extends AbstractObjectLoader<WorkflowTask> {

	@Autowired
	private WorkflowStepLoader workflowStepLoader;

	@Autowired
	private JpaDao jpaDao;

	@Override
	protected Class<? extends WorkflowTask> getType() {
		return WorkflowTask.class;
	}

	@Override
	public WorkflowTask ensureLoaded(WorkflowTask object) {
		WorkflowTask workflowTask = load(object);
		if (workflowTask != null) {
			WorkflowStep workflowStep = jpaDao.getFirstResultOrNull(WorkflowStep.class, (root, cq, cb) ->
					cb.and(
							cb.equal(root.get(WorkflowStep_.name), workflowTask.getStepName()),
							cb.equal(root.get(WorkflowStep_.workflowVersion).get(WorkflowVersion_.version), workflowTask.getVersion()),
							cb.equal(
									root.get(WorkflowStep_.workflowVersion).get(WorkflowVersion_.workflow).get(Workflow_.name),
									workflowTask.getWorkflowName()
							)
					)
			);
			workflowStepLoader.ensureLoaded(workflowStep);
		}
		return workflowTask;
	}

}
