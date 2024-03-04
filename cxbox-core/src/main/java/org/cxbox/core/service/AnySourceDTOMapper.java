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
import org.cxbox.constgen.DtoField;
import org.cxbox.core.crudma.CrudmaActionHolder;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.dto.mapper.AnySourceDtoConstructorService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnySourceDTOMapper {

	//	private final ApplicationEventPublisher applicationEventPublisher;
	private final AnySourceDtoConstructorService dtoConstructorService;

	//	private final TransactionService txService;
	private final Optional<ExtendedDtoFieldLevelSecurityService> extendedDtoFieldLevelSecurityService;

	private final DTOSecurityUtils dtoSecurityUtils;

	/**
	 * Creates a dto with the required set of fields for the current screen
	 */
	public <E, D extends DataResponseDTO> D entityToDto(BcIdentifier bc, E entity, Class<D> dtoClass,
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
	public <E, D extends DataResponseDTO> D entityToDto(BcIdentifier bc, E entity, Class<D> dtoClass,
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
	public <E, D extends DataResponseDTO> D entityToDto(BcIdentifier bc, E entity, Class<D> dtoClass) {
		return entityToDto(bc, entity, dtoClass, isFlushRequired(), emptyMap());
	}

	/**
	 * Creates a dto with the required set of fields for the current screen
	 */
	public <E, D extends DataResponseDTO> D entityToDto(BcIdentifier bc, E entity, Class<D> dtoClass,
			Map<String, Object> attributes) {
		return entityToDto(bc, entity, dtoClass, isFlushRequired(), attributes);
	}

	/**
	 * Creates a dto with a complete set of fields
	 */
	public <E, D extends DataResponseDTO> D entityToDto(E entity, Class<D> dtoClass) {
		return entityToDto(entity, dtoClass, dtoSecurityUtils.getDtoFields(dtoClass), isFlushRequired(), emptyMap());
	}

	/**
	 * Creates a dto with a given set of fields
	 */
	public <E, D extends DataResponseDTO> D entityToDto(E entity, Class<D> dtoClass,
			Set<DtoField<D, ?>> fields, boolean flushRequired) {
		return entityToDto(entity, dtoClass, fields, flushRequired, emptyMap());
	}

	public <E, D extends DataResponseDTO> D entityToDto(E entity, Class<D> dtoClass,
			DtoField<D, ?> field) {
		return entityToDto(entity, dtoClass, Collections.singleton(field));
	}

	/**
	 * Creates a dto with a given set of fields
	 */
	public <E, D extends DataResponseDTO> D entityToDto(E entity, Class<D> dtoClass,
			Set<DtoField<D, ?>> fields) {
		return entityToDto(entity, dtoClass, fields, isFlushRequired());
	}

	private <E, D extends DataResponseDTO> D entityToDto(E entity, Class<D> dtoClass,
			Set<DtoField<D, ?>> fields, boolean flushRequired, final Map<String, Object> attributes) {
		if (flushRequired) {
//			applicationEventPublisher.publishEvent(new EntitySerializationEvent(this, (BaseEntity) entity));
		}
		D result = createDto(entity, dtoClass, fields, attributes);
		setVstamp(result, entity);
		return result;
	}

	private <E, D extends DataResponseDTO> D createDto(E entity, Class<D> dtoClass,
			Set<DtoField<D, ?>> dtoFields, final Map<String, Object> attributes) {
		return dtoConstructorService.create(entity, dtoClass, dtoFields, attributes);
	}

	private <E> void setVstamp(Object dto, E entity) {
		if (!(dto instanceof DataResponseDTO)) {
			return;
		}
		DataResponseDTO responseDTO = (DataResponseDTO) dto;
		//TODO придумать как взять из AnySourceVersionAwareResponseService
		responseDTO.setVstamp(0L);
//		responseDTO.setVstamp(entity.getVstamp());
//		txService.invokeAfterCompletion(Invoker.of(() -> responseDTO.setVstamp(entity.getVstamp())));
	}

	private boolean isFlushRequired() {
		CrudmaActionType action = CrudmaActionHolder.getActionType();
		return action != null && action.isFlushRequired();
	}

}
