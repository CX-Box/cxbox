
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

package org.cxbox.source.dto;

import com.google.common.collect.ImmutableMap;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.mapper.DtoConstructor;
import org.cxbox.core.dto.mapper.ValueSupplier;
import org.cxbox.engine.workflow.dao.WorkflowableTaskDao;
import org.cxbox.model.workflow.entity.WorkflowTransitionHistory;
import org.cxbox.model.workflow.entity.WorkflowableTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.cxbox.source.dto.WorkflowTransitionHistoryDto_.*;

@Service
public class WorkflowTransitionHistoryDtoConstructor extends
		DtoConstructor<WorkflowTransitionHistory, WorkflowTransitionHistoryDto> {

	@Autowired
	private WorkflowableTaskDao<?> workflowableTaskDao;
	private final ValueSupplier<WorkflowTransitionHistory, WorkflowTransitionHistoryDto, WorkflowableTask> task = (mapping, entity) -> {
		return workflowableTaskDao.getTask(entity.getWorkflowTask());
	};

	public WorkflowTransitionHistoryDtoConstructor() {
		super(WorkflowTransitionHistory.class, WorkflowTransitionHistoryDto.class);
	}

	@Override
	protected Map<DtoField<? super WorkflowTransitionHistoryDto, ?>, ValueSupplier<? super WorkflowTransitionHistory, ? super WorkflowTransitionHistoryDto, ?>> buildValueSuppliers() {
		return ImmutableMap.<DtoField<? super WorkflowTransitionHistoryDto, ?>, ValueSupplier<? super WorkflowTransitionHistory, ? super WorkflowTransitionHistoryDto, ?>>builder()
				.put(createdDate, (mapping, entity) -> entity.getCreatedDate())
				.put(updatedDate, (mapping, entity) -> entity.getUpdatedDate())
				.put(transition, (mapping, entity) -> entity.getTransitionName())
				.put(transitionUser, (mapping, entity) -> entity.getTransitionUser().getUserNameInitials())
				.put(transitionPreviousUser, (mapping, entity) -> entity.getPreviousAssignee().getUserNameInitials())
				.put(transitionTaskId, (mapping, entity) -> mapping.get(task)
						.map(WorkflowableTask::getId)
						.orElse(null)
				)
				.put(stepTerm, (mapping, entity) -> mapping.get(task)
						.map(WorkflowableTask::getStepTerm)
						.orElse(null)
				)
				.build();
	}

}
