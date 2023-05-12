
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

import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.data.view.BcDTO;
import org.cxbox.core.dto.data.view.BcDTO_;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.crudma.api.BcService;
import org.cxbox.crudma.meta.BcFieldMetaBuilder;
import org.cxbox.model.ui.entity.Bc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


@Service
public class BcServiceImpl extends VersionAwareResponseService<BcDTO, Bc> implements BcService {

	@Lazy
	@Autowired
	private BcRegistry bcRegistry;

	@Autowired
	private TransactionService txService;

	public BcServiceImpl() {
		super(BcDTO.class, Bc.class, null, BcFieldMetaBuilder.class);
	}

	@Override
	protected CreateResult<BcDTO> doCreateEntity(final Bc entity, final BusinessComponent bc) {
		Long id = baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, baseDAO.findById(Bc.class, id)));
	}

	@Override
	protected ActionResultDTO<BcDTO> doUpdateEntity(Bc entity, BcDTO data, BusinessComponent bc) {
		if (data.hasChangedFields()) {
			if (data.isFieldChanged(BcDTO_.name)) {
				entity.setName(data.getName());
			}
			if (data.isFieldChanged(BcDTO_.parentName)) {
				entity.setParentName(data.getParentName());
			}
			if (data.isFieldChanged(BcDTO_.query)) {
				entity.setQuery(data.getQuery());
			}
			if (data.isFieldChanged(BcDTO_.defaultOrder)) {
				entity.setDefaultOrder(data.getDefaultOrder());
			}
			if (data.isFieldChanged(BcDTO_.reportDateField)) {
				entity.setReportDateField(data.getReportDateField());
			}
			if (data.isFieldChanged(BcDTO_.pageLimit)) {
				entity.setPageLimit(data.getPageLimit());
			}
			if (data.isFieldChanged(BcDTO_.binds)) {
				entity.setBinds(data.getBinds());
			}
		}
		txService.invokeAfterCompletion(Invoker.of(bcRegistry::refresh));
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

}
