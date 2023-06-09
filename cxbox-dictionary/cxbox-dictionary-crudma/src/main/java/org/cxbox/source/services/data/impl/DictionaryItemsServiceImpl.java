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


import static org.cxbox.api.util.i18n.LocalizationFormatter.uiMessage;

import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.model.dictionary.entity.DictionaryItem;
import org.cxbox.model.dictionary.entity.DictionaryItem_;
import org.cxbox.model.dictionary.entity.DictionaryTypeDesc;
import org.cxbox.model.dictionary.entity.DictionaryTypeDesc_;
import org.cxbox.source.DictionaryServiceAssociation;
import org.cxbox.source.dto.DictionaryItemDTO;
import org.cxbox.source.dto.DictionaryItemDTO_;
import org.cxbox.source.services.data.DictionaryItemService;
import org.cxbox.source.services.meta.DictionaryItemsFieldMetaBuilder;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class DictionaryItemsServiceImpl extends
		VersionAwareResponseService<DictionaryItemDTO, DictionaryItem> implements
		DictionaryItemService {

	@Autowired
	private DictionaryCache dictionaryCache;

	public DictionaryItemsServiceImpl() {
		super(DictionaryItemDTO.class, DictionaryItem.class, null, DictionaryItemsFieldMetaBuilder.class);
	}

	@Override
	protected Specification<DictionaryItem> getParentSpecification(BusinessComponent bc) {
		if (DictionaryServiceAssociation.adminDictionaryItem.isBc(bc)) {
			return (root, cq, cb) -> cb.and(
					cb.equal(
							root.get(DictionaryItem_.dictionaryTypeId).get(DictionaryTypeDesc_.id),
							bc.getParentIdAsLong()
					)
			);
		} else {
			return (root, cq, cb) -> cb.and();
		}
	}

	@Override
	protected ActionResultDTO<DictionaryItemDTO> doUpdateEntity(DictionaryItem item, DictionaryItemDTO data,
			BusinessComponent bc) {
		DictionaryTypeDesc dictionaryTypeDesc = baseDAO
				.findById(DictionaryTypeDesc.class, item.getDictionaryTypeId().getId());
		item.setType(dictionaryTypeDesc.getType());
		if (data.isFieldChanged(DictionaryItemDTO_.key)) {
			item.setKey(data.getKey());
		}
		if (data.isFieldChanged(DictionaryItemDTO_.active)) {
			item.setActive(data.getActive());
		}
		if (data.isFieldChanged(DictionaryItemDTO_.displayOrder)) {
			item.setDisplayOrder(data.getDisplayOrder());
		}
		if (data.isFieldChanged(DictionaryItemDTO_.description)) {
			item.setDescription(data.getDescription());
		}
		if (data.isFieldChanged(DictionaryItemDTO_.additionFlg)) {
			item.setAdditionFlg(data.isAdditionFlg());
		}
		DictionaryItemDTO updatedDto = entityToDto(bc, item);
		return new ActionResultDTO<>(updatedDto);
	}

	@Override
	protected CreateResult<DictionaryItemDTO> doCreateEntity(final DictionaryItem entity, final BusinessComponent bc) {
		DictionaryTypeDesc dictionaryTypeDesc = baseDAO.findById(DictionaryTypeDesc.class, bc.getParentIdAsLong());
		entity.setDictionaryTypeId(dictionaryTypeDesc);
		entity.setType(dictionaryTypeDesc.getType());
		return new CreateResult<>(entityToDto(bc, baseDAO.findById(DictionaryItem.class, baseDAO.save(entity))));
	}

	@Override
	public Actions<DictionaryItemDTO> getActions() {
		return Actions.<DictionaryItemDTO>builder()
				.create().add()
				.save().add()
				.delete().add()
				.action("reload-cache", uiMessage("action.clearCache")).invoker(this::actionReloadCache).add()
				.build();
	}

	private ActionResultDTO<DictionaryItemDTO> actionReloadCache(final BusinessComponent bc,
			final DictionaryItemDTO data) {
		dictionaryCache.reload();
		return new ActionResultDTO<>();
	}

	@Override
	public List<DictionaryItemDTO> reloadCache() {
		dictionaryCache.reload();
		List<DictionaryItem> entities = baseDAO.getList(DictionaryItem.class);
		return entities.stream().map(DictionaryItemDTO::new).collect(Collectors.toList());
	}

}
