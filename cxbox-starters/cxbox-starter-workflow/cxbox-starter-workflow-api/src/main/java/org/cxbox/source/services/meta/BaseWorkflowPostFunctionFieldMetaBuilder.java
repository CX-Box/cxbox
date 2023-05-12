
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

package org.cxbox.source.services.meta;

import static org.cxbox.source.dto.WorkflowPostFunctionDto_.actionCd;
import static org.cxbox.source.dto.WorkflowPostFunctionDto_.seq;
import static org.cxbox.source.dto.WorkflowPostFunctionDto_.stepTerm;

import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dict.WorkflowDictionaryType;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.source.dto.WorkflowPostFunctionDto;


public abstract class BaseWorkflowPostFunctionFieldMetaBuilder<D extends WorkflowPostFunctionDto> extends
		FieldMetaBuilder<D> {

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<D> fields, InnerBcDescription bcDescription, Long id,
			Long parentId) {
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<D> fields, InnerBcDescription bcDescription, Long parRowId) {
		fields.setForceActive(actionCd);
		fields.setDictionaryTypeWithAllValues(actionCd, WorkflowDictionaryType.WF_TRN_ACT);
		fields.setEnabled(seq, actionCd, stepTerm);
	}

}
