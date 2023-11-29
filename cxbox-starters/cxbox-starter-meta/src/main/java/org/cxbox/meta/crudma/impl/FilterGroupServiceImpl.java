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

package org.cxbox.meta.crudma.impl;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.meta.data.FilterGroupDTO;
import org.cxbox.meta.data.FilterGroupDTO_;
import org.cxbox.core.service.action.Actions;
import org.cxbox.meta.crudma.api.FilterGroupService;
import org.cxbox.meta.crudma.config.CoreServiceAssociation;
import org.cxbox.meta.crudma.meta.FilterGroupMetaBuilder;
import org.cxbox.meta.entity.FilterGroup;
import org.springframework.stereotype.Service;

@Service
public class FilterGroupServiceImpl extends VersionAwareResponseService<FilterGroupDTO, FilterGroup> implements
		FilterGroupService {

	protected FilterGroupServiceImpl() {
		super(FilterGroupDTO.class, FilterGroup.class, null, FilterGroupMetaBuilder.class);
	}

	@Override
	protected ActionResultDTO<FilterGroupDTO> doUpdateEntity(FilterGroup filterGroup, FilterGroupDTO data,
			BusinessComponent bc) {
		if (data.hasChangedFields()) {
			if (data.isFieldChanged(FilterGroupDTO_.name)) {
				filterGroup.setName(data.getName());
			}
			if (data.isFieldChanged(FilterGroupDTO_.filters)) {
				filterGroup.setFilters(data.getFilters());
			}
			if (data.isFieldChanged(FilterGroupDTO_.bc)) {
				filterGroup.setBc(data.getBc());
			}
		}
		return new ActionResultDTO<>(entityToDto(bc, filterGroup));
	}

	@Override
	protected CreateResult<FilterGroupDTO> doCreateEntity(final FilterGroup entity, final BusinessComponent bc) {
		entity.setName("Введите имя группы фильтров");
		entity.setFilters("Ведите фильтры");
		entity.setBc("Ведите имя бизнес компонента");
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	public Actions<FilterGroupDTO> getActions() {
		return Actions.<FilterGroupDTO>builder()
				.create().available(this::isAvailable).add()
				.save().available(this::isAvailable).add()
				.delete().available(this::isAvailable).add()
				.build();
	}

	private boolean isAvailable(BusinessComponent bc) {
		return CoreServiceAssociation.filterGroup.isBc(bc);
	}

}
