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

package org.cxbox.core.service;

import static java.util.Collections.emptyMap;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.ExtendedDtoFieldLevelSecurityService;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.crudma.CrudmaActionHolder;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.dto.mapper.DtoConstructorService;
import org.cxbox.model.core.api.EntitySerializationEvent;
import org.cxbox.model.core.entity.BaseEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class DTOMapper {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final DtoConstructorService dtoConstructorService;

	private final TransactionService txService;

	private final Optional<ExtendedDtoFieldLevelSecurityService> extendedDtoFieldLevelSecurityService;

	private final DTOSecurityUtils dtoSecurityUtils;

	/**
	 * Creates a dto with the required set of fields for the current screen
	 */
	public <E extends BaseEntity, D extends DataResponseDTO> D entityToDto(BcIdentifier bc, E entity, Class<D> dtoClass,
			boolean flushRequired, Map<String, Object> attributes) {
		return entityToDto(
				entity,
				dtoClass,
				getDtoFieldsAvailableOnCurrentScreen(bc, dtoClass, true),
				flushRequired,
				attributes
		);
	}

	/**
	 * Creates a dto with the required set of fields for the current screen
	 */
	public <E extends BaseEntity, D extends DataResponseDTO> D entityToDto(BcIdentifier bc, E entity, Class<D> dtoClass,
			boolean flushRequired) {
		return entityToDto(
				entity,
				dtoClass,
				getDtoFieldsAvailableOnCurrentScreen(bc, dtoClass, true),
				flushRequired,
				emptyMap()
		);
	}

	private <D extends DataResponseDTO> Set<DtoField<D, ?>> getDtoFieldsAvailableOnCurrentScreen(BcIdentifier bc,
			Class<D> dtoClass, boolean visibleOnly) {
		if (visibleOnly && extendedDtoFieldLevelSecurityService.isPresent()) {
			return extendedDtoFieldLevelSecurityService.get().getDtoFieldsAvailableOnCurrentScreen(bc);
		}
		return dtoSecurityUtils.getDtoFields(dtoClass);
	}

	/**
	 * Creates a dto with the required set of fields for the current screen
	 */
	public <E extends BaseEntity, D extends DataResponseDTO> D entityToDto(BcIdentifier bc, E entity, Class<D> dtoClass) {
		return entityToDto(bc, entity, dtoClass, isFlushRequired(), emptyMap());
	}

	/**
	 * Creates a dto with the required set of fields for the current screen
	 */
	public <E extends BaseEntity, D extends DataResponseDTO> D entityToDto(BcIdentifier bc, E entity, Class<D> dtoClass,
			Map<String, Object> attributes) {
		return entityToDto(bc, entity, dtoClass, isFlushRequired(), attributes);
	}

	/**
	 * Creates a dto with a complete set of fields
	 */
	public <E extends BaseEntity, D extends DataResponseDTO> D entityToDto(E entity, Class<D> dtoClass) {
		return entityToDto(entity, dtoClass, dtoSecurityUtils.getDtoFields(dtoClass), isFlushRequired(), emptyMap());
	}

	/**
	 * Creates a dto with a given set of fields
	 */
	public <E extends BaseEntity, D extends DataResponseDTO> D entityToDto(E entity, Class<D> dtoClass,
			Set<DtoField<D, ?>> fields, boolean flushRequired) {
		return entityToDto(entity, dtoClass, fields, flushRequired, emptyMap());
	}

	public <E extends BaseEntity, D extends DataResponseDTO> D entityToDto(E entity, Class<D> dtoClass,
			DtoField<D, ?> field) {
		return entityToDto(entity, dtoClass, Collections.singleton(field));
	}

	/**
	 * Creates a dto with a given set of fields
	 */
	public <E extends BaseEntity, D extends DataResponseDTO> D entityToDto(E entity, Class<D> dtoClass,
			Set<DtoField<D, ?>> fields) {
		return entityToDto(entity, dtoClass, fields, isFlushRequired());
	}

	private <E extends BaseEntity, D extends DataResponseDTO> D entityToDto(E entity, Class<D> dtoClass,
			Set<DtoField<D, ?>> fields, boolean flushRequired, final Map<String, Object> attributes) {
		if (flushRequired) {
			sendSerializationEvent(entity);
		}
		D result = createDto(entity, dtoClass, fields, attributes);
		setVstamp(result, entity);
		return result;
	}

	private <E extends BaseEntity, D extends DataResponseDTO> D createDto(E entity, Class<D> dtoClass,
			Set<DtoField<D, ?>> dtoFields, final Map<String, Object> attributes) {
		return dtoConstructorService.create(entity, dtoClass, dtoFields, attributes);
	}

	private void setVstamp(Object dto, BaseEntity entity) {
		if (!(dto instanceof DataResponseDTO)) {
			return;
		}
		DataResponseDTO responseDTO = (DataResponseDTO) dto;
		responseDTO.setVstamp(entity.getVstamp());
		txService.invokeAfterCompletion(Invoker.of(() -> responseDTO.setVstamp(entity.getVstamp())));
	}

	private void sendSerializationEvent(BaseEntity entity) {
		applicationEventPublisher.publishEvent(new EntitySerializationEvent(this, entity));
	}

	private boolean isFlushRequired() {
		CrudmaActionType action = CrudmaActionHolder.getActionType();
		return action != null && action.isFlushRequired();
	}

}
