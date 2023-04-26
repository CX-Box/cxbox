/*-
 * #%L
 * IO Cxbox - Workflow API
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

package org.cxbox.source.services.data.impl;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.model.workflow.entity.WorkflowCondition;
import org.cxbox.source.dto.WorkflowConditionDto;
import org.cxbox.source.services.data.WorkflowConditionService;
import org.cxbox.source.services.meta.WorkflowConditionFieldMetaBuilder;
import org.springframework.stereotype.Service;

@Service
public class WorkflowConditionServiceImpl extends
		BaseWorkflowConditionServiceImpl<WorkflowConditionDto, WorkflowCondition> implements WorkflowConditionService {

	public WorkflowConditionServiceImpl() {
		super(WorkflowConditionDto.class, WorkflowCondition.class, null, WorkflowConditionFieldMetaBuilder.class);
	}

	@Override
	protected WorkflowCondition create(BusinessComponent bc) {
		return new WorkflowCondition();
	}

}
