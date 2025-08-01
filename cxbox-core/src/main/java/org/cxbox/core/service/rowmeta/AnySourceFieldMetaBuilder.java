/*
 * © OOO "SI IKS LAB", 2022-2024
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
import org.cxbox.constgen.DtoField;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.PlatformRequest;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.AnySourceBcDescription;
import org.cxbox.core.crudma.bc.impl.ExtremeBcDescription;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.external.core.ParentDtoFirstLevelCache;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AnySourceFieldMetaBuilder<T extends DataResponseDTO> {

	@Autowired
	private PlatformRequest platformRequest;

	@Autowired
	private ParentDtoFirstLevelCache parentDtoFirstLevelCache;

	public void buildRowDependentMeta(RowDependentFieldsMeta<T> fields, BusinessComponent bc) {
		if (bc.getDescription() instanceof InnerBcDescription) {
			buildRowDependentMeta(fields, bc.getDescription(), bc.getId(), bc.getParentId());
		} else if (bc.getDescription() instanceof AnySourceBcDescription) {
			buildRowDependentMeta(fields, bc.getDescription(), bc.getId(), bc.getParentId());
		} else if (bc.getDescription() instanceof ExtremeBcDescription) {
			buildExtremeRowDependentMeta(fields, bc.getDescription(), bc.getId(), bc.getParentId());
		}
	}

	public void buildIndependentMeta(FieldsMeta<T> fields, BusinessComponent bc) {
		buildIndependentMeta(fields, bc.getDescription(), bc.getParentId());
	}

	public abstract void buildRowDependentMeta(RowDependentFieldsMeta<T> fields, BcDescription bcDescription,
			String id, String parentId);

	public void buildExtremeRowDependentMeta(RowDependentFieldsMeta<T> fields, BcDescription bcDescription,
			String id, String parentId) {
	}

	public abstract void buildIndependentMeta(FieldsMeta<T> fields, BcDescription bcDescription, String parentId);

	public <P extends DataResponseDTO, F> F getParentField(DtoField<P, F> dtoField, BusinessComponent bc) {
		return parentDtoFirstLevelCache.getParentField(dtoField, bc);
	}

	public CrudmaActionType getActionType() {
		return platformRequest.getCrudmaActionType();
	}

	public BusinessComponent getBc() {
		return platformRequest.getBc();
	}


}
