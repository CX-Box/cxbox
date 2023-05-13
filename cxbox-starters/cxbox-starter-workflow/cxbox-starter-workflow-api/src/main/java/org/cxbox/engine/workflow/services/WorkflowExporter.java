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

package org.cxbox.engine.workflow.services;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.api.file.entity.CxboxFile;
import org.cxbox.model.workflow.entity.WorkflowVersion;
import org.cxbox.source.dto.WorkflowVersionDto;


public interface WorkflowExporter {

	CxboxFile exportNewVersion(BusinessComponent bc, WorkflowVersionDto data);

	WorkflowVersion copyNewVersion(BusinessComponent bc, WorkflowVersionDto data);

}
