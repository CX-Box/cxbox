/*-
 * #%L
 * IO Cxbox - Source
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

package org.cxbox.crudma.impl;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.service.action.Actions;
import org.cxbox.crudma.api.BcPropertiesService;
import org.cxbox.crudma.dto.BcPropertiesDTO;
import org.cxbox.crudma.dto.BcPropertiesDTO_;
import org.cxbox.crudma.meta.BcPropertiesMetaBuilder;
import org.cxbox.model.ui.entity.BcProperties;
import org.cxbox.model.ui.entity.BcProperties_;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BcPropertiesServiceImpl extends VersionAwareResponseService<BcPropertiesDTO, BcProperties> implements
		BcPropertiesService {

	protected BcPropertiesServiceImpl() {
		super(BcPropertiesDTO.class, BcProperties.class, null, BcPropertiesMetaBuilder.class);
	}

	@Override
	protected CreateResult<BcPropertiesDTO> doCreateEntity(final BcProperties entity, final BusinessComponent bc) {
		entity.setBc("Ведите имя бизнес компонента");
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	protected ActionResultDTO<BcPropertiesDTO> doUpdateEntity(BcProperties bcProperties, BcPropertiesDTO data,
			BusinessComponent bc) {
		String bcName = data.getBc();
		List<BcProperties> existDefaultBcProperties = baseDAO.getList(BcProperties.class, BcProperties_.bc, bcName);
		if (!existDefaultBcProperties.isEmpty()) {
			throw new BusinessException().addPopup(errorMessage("error.bc_settings_already_exist", bc));
		}
		if (data.hasChangedFields()) {
			if (data.isFieldChanged(BcPropertiesDTO_.bc)) {
				bcProperties.setBc(bcName);
			}
			if (data.isFieldChanged(BcPropertiesDTO_.filter)) {
				bcProperties.setFilter(data.getFilter());
			}
			if (data.isFieldChanged(BcPropertiesDTO_.sort)) {
				bcProperties.setSort(data.getSort());
			}
			if (data.isFieldChanged(BcPropertiesDTO_.limit)) {
				bcProperties.setLimit(data.getLimit());
			}
			if (data.isFieldChanged(BcPropertiesDTO_.reportPeriod)) {
				bcProperties.setReportPeriod(data.getReportPeriod());
			}
		}
		baseDAO.save(bcProperties);
		return new ActionResultDTO<>(entityToDto(bc, bcProperties));
	}

	@Override
	public Actions<BcPropertiesDTO> getActions() {
		return Actions.<BcPropertiesDTO>builder()
				.create().add()
				.save().add()
				.delete().add()
				.build();
	}

}
