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

package org.cxbox.source.service.data.impl;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;
import static org.cxbox.api.util.i18n.LocalizationFormatter.uiMessage;

import org.cxbox.core.bc.InnerBcTypeAware;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.service.ResponseService;
import org.cxbox.core.service.action.ActionAvailableChecker;
import org.cxbox.core.service.action.Actions;
import org.cxbox.model.dictionary.links.entity.CustomizableResponseService;
import org.cxbox.model.dictionary.links.entity.CustomizableResponseService_;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRule;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRule_;
import org.cxbox.source.dto.CustomizableResponseServiceDto;
import org.cxbox.source.dto.CustomizableResponseServiceDto_;
import org.cxbox.source.engine.LinkedDictionaryServiceImpl.LinkedDictionaryCache;
import org.cxbox.source.service.data.CustomizableResponseSrvsService;
import org.cxbox.source.service.meta.CustomizableResponseServiceFieldMetaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomizableRespSrvsServiceImpl extends
		VersionAwareResponseService<CustomizableResponseServiceDto, CustomizableResponseService> implements
		CustomizableResponseSrvsService {

	@Autowired
	private LinkedDictionaryCache linkedDictionaryCache;

	@Autowired
	private BcRegistry bcRegistry;

	@Autowired
	private InnerBcTypeAware innerBcTypeAware;

	public CustomizableRespSrvsServiceImpl() {
		super(
				CustomizableResponseServiceDto.class,
				CustomizableResponseService.class,
				null,
				CustomizableResponseServiceFieldMetaBuilder.class
		);
	}

	@Override
	public ActionResultDTO<CustomizableResponseServiceDto> deleteEntity(BusinessComponent bc) {
		if (bc.getIdAsLong() != null) {
			Long rulesCount = baseDAO.getCount(DictionaryLnkRule.class, (root, cq, cb) ->
					cb.equal(
							root.get(DictionaryLnkRule_.service).get(CustomizableResponseService_.id),
							bc.getIdAsLong()
					)
			);
			if (rulesCount > 0) {
				throw new BusinessException().addPopup(errorMessage("error.cant_delete_service_rules_exist"));
			}
		}
		return super.deleteEntity(bc);
	}

	@Override
	public Actions<CustomizableResponseServiceDto> getActions() {
		return Actions.<CustomizableResponseServiceDto>builder()
				.create().add()
				.save().add()
				.delete().add()
				.action("clearCache", uiMessage("action.clearCache"))
				.available(ActionAvailableChecker.ALWAYS_TRUE)
				.invoker((bc, data) -> {
					linkedDictionaryCache.evictRules();
					return new ActionResultDTO<>();
				}).add()
				.build();
	}

	@Override
	protected CreateResult<CustomizableResponseServiceDto> doCreateEntity(final CustomizableResponseService entity,
			final BusinessComponent bc) {
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	@Override
	protected ActionResultDTO<CustomizableResponseServiceDto> doUpdateEntity(CustomizableResponseService entity,
			CustomizableResponseServiceDto data, BusinessComponent bc) {
		if (data.isFieldChanged(CustomizableResponseServiceDto_.serviceName)) {
			if (!entity.getRules().isEmpty()) {
				throw new BusinessException().addPopup(errorMessage("error.cant_modify_service_rules_exist"));
			}
			entity.setServiceName(data.getServiceName());

			InnerBcDescription bcDescription = bcRegistry.select(InnerBcDescription.class)
					.distinct()
					.filter(innerBc -> innerBc != null && innerBc.getServiceClass().getSimpleName()
							.equals(data.getServiceName()))
					.findFirst().orElse(null);

			if (bcDescription != null && ResponseService.class.isAssignableFrom(bcDescription.getServiceClass())) {
				entity.setDtoClass(innerBcTypeAware.getTypeOfDto(bcDescription).getName());
			}
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

}
