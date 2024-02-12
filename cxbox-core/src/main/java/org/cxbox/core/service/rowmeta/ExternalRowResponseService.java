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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.rowmeta.ActionsDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.dto.rowmeta.EngineFieldsMeta;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.dto.rowmeta.RowMetaDTO;
import org.cxbox.core.service.ExternalResponseService;
import org.cxbox.core.service.linkedlov.LinkedDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ExternalRowResponseService extends RowResponseService {

	@Autowired
	public ApplicationContext ctx;

	@Autowired
	public Optional<List<BcDisabler>> bcDisablersList;

	public final Map<String, List<BcDisabler>> bcDisablers = new HashMap<>();

	//TODO сделать getter
	@PostConstruct
	public void init() {
		bcDisablersList.ifPresent(disablers -> {
			for (final BcDisabler bcDisabler : disablers) {
				for (final BcIdentifier bcIdentifier : bcDisabler.getSupportedBc()) {
					this.bcDisablers.computeIfAbsent(bcIdentifier.getName(), s -> new ArrayList<>()).add(bcDisabler);
				}
			}
		});
	}

	public ExternalRowResponseService(ApplicationContext ctx, Optional<List<BcDisabler>> bcDisablers,
			Optional<LinkedDictionaryService> linkedDictionaryService, BcUtils bcUtils, ObjectMapper objectMapper) {
		super(ctx, bcDisablers, linkedDictionaryService, bcUtils, objectMapper);
	}

	public MetaDTO getExternalResponse(RowMetaType type, DataResponseDTO dataDTO, BusinessComponent bc,
			ExternalResponseService<?, ?> responseService) {
		ActionsDTO actionDTO = responseService.getAvailableActions(type, dataDTO, bc);
		return getExternalResponse(type, dataDTO, bc, actionDTO, responseService.getExternalFieldMetaBuilder());
	}

	public MetaDTO getExternalResponse(RowMetaType type, DataResponseDTO dataDTO, BusinessComponent bc, ActionsDTO actionDTO,
			Class<? extends ExternalFieldMetaBuilder> fieldMetaBuilder) {
		EngineFieldsMeta fieldsNode = getMeta(bc, type, dataDTO, true);
		if (fieldMetaBuilder != null && type != RowMetaType.META_EMPTY) {
			ExternalFieldMetaBuilder builder = ctx.getBean(fieldMetaBuilder);
			builder.buildIndependentMeta(fieldsNode, bc);
			if (bc.getId() != null) {
				builder.buildRowDependentMeta(fieldsNode, bc);
			}
		}

		for (final BcDisabler bcDisabler : bcDisablers.getOrDefault(bc.getName(), Collections.emptyList())) {
			if (bcDisabler.isBcDisabled(bc)) {
				fieldsNode.disableFields();
				bcDisabler.disableActions(actionDTO);
			}
		}

		return new MetaDTO(new RowMetaDTO(actionDTO, fieldsNode));
	}

	public MetaDTO getExternalResponse(RowMetaType type, CreateResult createResult, BusinessComponent bc,
			ExternalResponseService<?, ?> responseService) {
		final DataResponseDTO newRecord = createResult.getRecord();
		final BusinessComponent newBc = bc.withId(newRecord.getId());
		MetaDTO metaDTO = getExternalResponse(type, newRecord, newBc, responseService);
		metaDTO.setPostActions(createResult.getPostActions());
		return metaDTO;
	}

}
