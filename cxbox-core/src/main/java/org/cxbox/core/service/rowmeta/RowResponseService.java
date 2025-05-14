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

package org.cxbox.core.service.rowmeta;

import static org.cxbox.core.service.rowmeta.RowMetaType.META;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cxbox.api.ExtendedDtoFieldLevelSecurityService;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.DataResponseDTO_;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.core.config.properties.WidgetFieldsIdResolverProperties;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.rowmeta.ActionsDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.dto.rowmeta.EngineFieldsMeta;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.dto.rowmeta.RowMetaDTO;
import org.cxbox.core.service.ResponseService;
import org.cxbox.core.service.linkedlov.LinkedDictionaryService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.cxbox.api.util.CxReflectionUtils;
import org.cxbox.dictionary.DictionaryProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class RowResponseService {

	private final ApplicationContext ctx;

	private final LinkedDictionaryService linkedDictionaryService;

	private final Optional<ExtendedDtoFieldLevelSecurityService> extendedDtoFieldLevelSecurityService;

	private final Map<String, List<BcDisabler>> bcDisablers;

	private final ObjectMapper objectMapper;

	private final Optional<DictionaryProvider> dictionaryProvider;

	private final WidgetFieldsIdResolverProperties properties;

	public RowResponseService(ApplicationContext ctx,
			Optional<List<BcDisabler>> bcDisablers,
			Optional<LinkedDictionaryService> linkedDictionaryService,
			Optional<ExtendedDtoFieldLevelSecurityService> extendedDtoFieldLevelSecurityService,
			Optional<DictionaryProvider> dictionaryProvider,
			WidgetFieldsIdResolverProperties properties,
			@Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objectMapper) {
		this.ctx = ctx;
		this.linkedDictionaryService = linkedDictionaryService.orElse(null);
		this.extendedDtoFieldLevelSecurityService = extendedDtoFieldLevelSecurityService;
		this.bcDisablers = new HashMap<>();
		this.objectMapper = objectMapper;
		this.dictionaryProvider = dictionaryProvider;
		this.properties = properties;
		bcDisablers.ifPresent(disablers -> {
			for (final BcDisabler bcDisabler : disablers) {
				for (final BcIdentifier bcIdentifier : bcDisabler.getSupportedBc()) {
					this.bcDisablers.computeIfAbsent(bcIdentifier.getName(), s -> new ArrayList<>()).add(bcDisabler);
				}
			}
		});
	}

	public MetaDTO getResponse(RowMetaType type, DataResponseDTO dataDTO, BusinessComponent bc,
			ResponseService<?, ?> responseService) {
		ActionsDTO actionDTO = responseService.getAvailableActions(type, dataDTO, bc);
		return getResponse(type, dataDTO, bc, actionDTO, responseService.getFieldMetaBuilder());
	}

	public MetaDTO getResponse(RowMetaType type, CreateResult createResult, BusinessComponent bc,
			ResponseService<?, ?> responseService) {
		final DataResponseDTO newRecord = createResult.getRecord();
		final BusinessComponent newBc = bc.withId(newRecord.getId());
		MetaDTO metaDTO = getResponse(type, newRecord, newBc, responseService);
		metaDTO.setPostActions(createResult.getPostActions());
		return metaDTO;
	}

	public MetaDTO getResponse(RowMetaType type, DataResponseDTO dataDTO, BusinessComponent bc, ActionsDTO actionDTO,
			Class<? extends FieldMetaBuilder> fieldMetaBuilder) {
		EngineFieldsMeta fieldsNode = getMeta(bc, type, dataDTO, true);
		if (linkedDictionaryService != null) {
			linkedDictionaryService.fillRowMetaWithLinkedDictionaries(
					fieldsNode,
					bc,
					dataDTO,
					type == RowMetaType.META_EMPTY
			);
		}
		//add changedNow for MetaBuilder
		if (dataDTO.getChangedNow() != null && !dataDTO.getChangedNow().isEmpty()) {
			fieldsNode.add(getDTOFromField(META, FieldUtils.getField(
					dataDTO.getClass(),
					DataResponseDTO_.changedNow.getName(),
					true
			), dataDTO));
		}

		if (fieldMetaBuilder != null && type != RowMetaType.META_EMPTY) {
			FieldMetaBuilder builder = ctx.getBean(fieldMetaBuilder);
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

	public MetaDTO getExtremeResponse(RowMetaType type, DataResponseDTO dataDTO, BusinessComponent bc,
			Class<? extends FieldMetaBuilder> fieldMetaBuilder, boolean visibleOnly) {
		EngineFieldsMeta fieldsNode = getMeta(bc, type, dataDTO, visibleOnly);
		if (fieldMetaBuilder != null) {
			FieldMetaBuilder builder = ctx.getBean(fieldMetaBuilder);
			builder.buildRowDependentMeta(fieldsNode, bc);
		}
		return new MetaDTO(new RowMetaDTO(new ActionsDTO(), fieldsNode));
	}

	public EngineFieldsMeta getMeta(BcIdentifier bc, RowMetaType type, DataResponseDTO dataDto, boolean visibleOnly) {
		final EngineFieldsMeta fieldsNode = new EngineFieldsMeta(objectMapper, dictionaryProvider);
		for (final String dtoField : getFields(bc, dataDto, visibleOnly)) {
			final Field field = FieldUtils.getField(dataDto.getClass(), dtoField, true);
			if (field == null) {
				continue;
			}
			final FieldDTO fieldDTO = getDTOFromField(type, field, dataDto);
			if (fieldDTO != null) {
				fieldsNode.add(fieldDTO);
			}
		}
		return fieldsNode;
	}

	private Set<String> getFields(BcIdentifier bc, DataResponseDTO dataDTO, boolean visibleOnly) {
		if (visibleOnly && extendedDtoFieldLevelSecurityService.isPresent()) {
			return extendedDtoFieldLevelSecurityService.get().getBcFieldsForCurrentScreen(bc);
		}
		return CxReflectionUtils.getAllNonSyntheticFieldsList(dataDTO.getClass()).stream()
				.map(Field::getName).collect(Collectors.toSet());
	}

	private FieldDTO getDTOFromField(RowMetaType type, Field field, DataResponseDTO dataDTO) {
		field.setAccessible(true);
		if (field.getAnnotation(JsonIgnore.class) != null) {
			return null;
		}
		FieldDTO fieldDTO = new FieldDTO(field);
		fieldDTO.setSortable(properties.isSortEnabledDefault());
		try {
			switch (type) {
				case META_NEW:
				case ON_FIELD_UPDATE_META:
				case META:
					fieldDTO.setCurrentValue(field.get(dataDTO));
					break;
				default:
					break;
			}
		} catch (IllegalAccessException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return fieldDTO;
	}

}
