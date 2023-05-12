
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

package org.cxbox.crudma.impl;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.data.SystemSettingDTO;
import org.cxbox.core.dto.data.SystemSettingDTO_;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.crudma.api.SystemSettingService;
import org.cxbox.crudma.meta.SystemSettingFieldMetaBuilder;
import org.cxbox.model.core.entity.SystemSetting;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingServiceImpl extends
		VersionAwareResponseService<SystemSettingDTO, SystemSetting> implements
		SystemSettingService {

	public SystemSettingServiceImpl() {
		super(SystemSettingDTO.class, SystemSetting.class, null, SystemSettingFieldMetaBuilder.class);
	}

	@Override
	protected Specification<SystemSetting> getParentSpecification(BusinessComponent bc) {
		return (root, cq, cb) -> cb.and();
	}

	@Override
	protected ActionResultDTO<SystemSettingDTO> doUpdateEntity(SystemSetting item, SystemSettingDTO data,
			BusinessComponent bc) {
		if (data.isFieldChanged(SystemSettingDTO_.key)) {
			item.setKey(data.getKey());
		}
		if (data.isFieldChanged(SystemSettingDTO_.value)) {
			item.setValue(data.getValue());
		}
		return new ActionResultDTO<>(entityToDto(bc, item));
	}

	@Override
	protected CreateResult<SystemSettingDTO> doCreateEntity(final SystemSetting entity, final BusinessComponent bc) {
		return new CreateResult<>(entityToDto(bc, baseDAO.findById(SystemSetting.class, baseDAO.save(entity))));
	}

	@Override
	public Actions<SystemSettingDTO> getActions() {
		return Actions.<SystemSettingDTO>builder()
				.create().add()
				.save().add()
				.delete().add()
				.build();
	}


}
