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

package org.cxbox.core.service.rowmeta;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.ExtremeBcDescription;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;


public abstract class FieldMetaBuilder<T extends DataResponseDTO> {

	public void buildRowDependentMeta(RowDependentFieldsMeta<T> fields, BusinessComponent bc) {
		if (bc.getDescription() instanceof InnerBcDescription) {
			buildRowDependentMeta(fields, bc.getDescription(), bc.getIdAsLong(), bc.getParentIdAsLong());
		} else if (bc.getDescription() instanceof ExtremeBcDescription) {
			buildExtremeRowDependentMeta(fields, bc.getDescription(), bc.getIdAsLong(), bc.getParentIdAsLong());
		}
	}

	public void buildIndependentMeta(FieldsMeta<T> fields, BusinessComponent bc) {
		buildIndependentMeta(fields, bc.getDescription(), bc.getParentIdAsLong());
	}

	public abstract void buildRowDependentMeta(RowDependentFieldsMeta<T> fields, InnerBcDescription bcDescription,
			Long id, Long parentId);

	public void buildExtremeRowDependentMeta(RowDependentFieldsMeta<T> fields, ExtremeBcDescription bcDescription,
			Long id, Long parentId) {
	}

	public abstract void buildIndependentMeta(FieldsMeta<T> fields, InnerBcDescription bcDescription, Long parentId);

}
