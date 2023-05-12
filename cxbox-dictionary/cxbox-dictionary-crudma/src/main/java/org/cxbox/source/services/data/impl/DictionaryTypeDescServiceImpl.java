
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

package org.cxbox.source.services.data.impl;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.model.dictionary.entity.DictionaryTypeDesc;
import org.cxbox.source.dto.DictionaryTypeDescDTO;
import org.cxbox.source.dto.DictionaryTypeDescDTO_;
import org.cxbox.source.services.data.DictionaryTypeDescService;
import org.cxbox.source.services.meta.DictionaryTypeDescFieldMetaBuilder;
import org.springframework.stereotype.Service;

@Service
public class DictionaryTypeDescServiceImpl extends
		VersionAwareResponseService<DictionaryTypeDescDTO, DictionaryTypeDesc> implements
		DictionaryTypeDescService {

	public DictionaryTypeDescServiceImpl() {
		super(DictionaryTypeDescDTO.class, DictionaryTypeDesc.class, null, DictionaryTypeDescFieldMetaBuilder.class);
	}

	@Override
	protected CreateResult<DictionaryTypeDescDTO> doCreateEntity(final DictionaryTypeDesc entity,
			final BusinessComponent bc) {
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	protected ActionResultDTO<DictionaryTypeDescDTO> doUpdateEntity(DictionaryTypeDesc item,
			DictionaryTypeDescDTO data, BusinessComponent bc) {
		if (data.isFieldChanged(DictionaryTypeDescDTO_.type)) {
			item.setType(data.getType());
		}
		if (data.isFieldChanged(DictionaryTypeDescDTO_.typeDesc)) {
			item.setTypeDesc(data.getTypeDesc());
		}
		return new ActionResultDTO<>(entityToDto(bc, item));
	}

	@Override
	public Actions<DictionaryTypeDescDTO> getActions() {
		return Actions.<DictionaryTypeDescDTO>builder()
				.create().add()
				.save().add()
				.build();
	}

}
