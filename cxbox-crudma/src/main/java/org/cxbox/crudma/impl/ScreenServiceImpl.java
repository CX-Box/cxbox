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
import org.cxbox.core.dto.data.view.ScreenDTO;
import org.cxbox.core.dto.data.view.ScreenDTO_;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.crudma.api.ScreenService;
import org.cxbox.crudma.meta.ScreenFieldMetaBuilder;
import org.cxbox.model.ui.entity.Screen;
import org.springframework.stereotype.Service;


@Service
public class ScreenServiceImpl extends VersionAwareResponseService<ScreenDTO, Screen> implements ScreenService {

	public ScreenServiceImpl() {
		super(ScreenDTO.class, Screen.class, null, ScreenFieldMetaBuilder.class);
	}

	@Override
	protected CreateResult<ScreenDTO> doCreateEntity(final Screen entity, final BusinessComponent bc) {
		Long id = baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, baseDAO.findById(Screen.class, id)));
	}

	@Override
	protected ActionResultDTO<ScreenDTO> doUpdateEntity(Screen screen, ScreenDTO data, BusinessComponent bc) {
		if (data.hasChangedFields()) {
			if (data.isFieldChanged(ScreenDTO_.name)) {
				screen.setName(data.getName());
			}
			if (data.isFieldChanged(ScreenDTO_.title)) {
				screen.setTitle(data.getTitle());
			}
			if (data.isFieldChanged(ScreenDTO_.primary)) {
				screen.setPrimary(data.getPrimary());
			}
			if (data.isFieldChanged(ScreenDTO_.primaries)) {
				screen.setPrimaries(data.getPrimaries());
			}
		}
		return new ActionResultDTO<>(entityToDto(bc, screen));
	}

}
