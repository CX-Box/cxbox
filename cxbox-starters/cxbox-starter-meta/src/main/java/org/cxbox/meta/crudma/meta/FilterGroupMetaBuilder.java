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

package org.cxbox.meta.crudma.meta;


import static org.cxbox.meta.data.FilterGroupDTO_.name;
import static org.cxbox.meta.data.FilterGroupDTO_.bc;
import static org.cxbox.meta.data.FilterGroupDTO_.filters;

import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.meta.data.FilterGroupDTO;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.springframework.stereotype.Service;

@Service
public class FilterGroupMetaBuilder extends FieldMetaBuilder<FilterGroupDTO> {

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<FilterGroupDTO> fields, InnerBcDescription bcDescription,
			Long rowId, Long parRowId) {
		fields.setEnabled(
				name,
				filters,
				bc
		);
		fields.setRequired(
				name,
				filters,
				bc
		);
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<FilterGroupDTO> fields, InnerBcDescription bcDescription, Long parRowId) {
	}

}
