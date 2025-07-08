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

package org.cxbox.core.service;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;
import static org.cxbox.core.controller.param.RequestParameters.CHANGED_NOW;
import static org.cxbox.core.controller.param.RequestParameters.DATA;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.DataResponseDTO.ChangedNowParam;
import org.cxbox.api.data.dto.DataResponseDTO_;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.AnySourceBcDescription;
import org.cxbox.core.dto.BusinessError.Entity;
import org.cxbox.core.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Factory for services creation
 */
@Service
@RequiredArgsConstructor
public class AnySourceResponseFactory {

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper mapper;

	private final ApplicationContext ctx;

	@Autowired
	private ValidatorsProvider validatorsProvider;

	@Autowired
	private ChangedNowValidationService changedNowValidationService;

	/**
	 * @param anySourceBcDescription information about BC;
	 * @return {@link AnySourceResponseService} common interface implemented by all services working with the controller
	 */
	public AnySourceResponseService getService(AnySourceBcDescription anySourceBcDescription) {
		return ctx.getBean(getServiceClass(anySourceBcDescription));
	}

	public DataResponseDTO getDTOFromMap(Map<String, Object> map, Class<?> clazz, BusinessComponent bc) {
		return getDTOFromMapDataAndChangedNow(map, clazz, bc, false);
	}

	public DataResponseDTO getDTOFromMapIgnoreBusinessErrors(Map<String, Object> map, Class<?> clazz,
			BusinessComponent bc) {
		return getDTOFromMapDataAndChangedNow(map, clazz, bc, true);
	}


	private DataResponseDTO getDTOFromMapDataAndChangedNow(Map<String, Object> map, Class<?> clazz, BusinessComponent bc,
			boolean ignoreBusinessErrors) {
		Map<String, Object> changedNowMap = (Map<String, Object>) map.get(CHANGED_NOW);
		Map<String, Object> dataMap = (Map<String, Object>) map.get(DATA);

		DataResponseDTO dataResponseDTO;
		if (changedNowMap == null) {
			dataResponseDTO = getDTOFromMapInner(dataMap == null ? map : dataMap, clazz, bc, ignoreBusinessErrors);
		} else {
			dataResponseDTO = getDTOFromMapInner(dataMap, clazz, bc, ignoreBusinessErrors);
			DataResponseDTO changedNowDTO = getDTOFromMapInner(changedNowMap,clazz,bc,ignoreBusinessErrors);

			ChangedNowParam changedNowParam = changedNowValidationService.buildCnangedNowParam(changedNowMap.keySet(),changedNowDTO);
			dataResponseDTO.setChangedNowParam(changedNowParam);
		}
		return dataResponseDTO;
	}

	private DataResponseDTO getDTOFromMapInner(Map<String, Object> map, Class<?> clazz, BusinessComponent bc,
			boolean ignoreBusinessErrors) {
		DtoDeserializationHandler handler = new DtoDeserializationHandler();
		mapper.addHandler(handler);
		Object objectResult;
		try {
			objectResult = mapper.convertValue(map, clazz);
		} catch (IllegalArgumentException e) {
			throw new BusinessException().addPopup(errorMessage("error.dto_deserialization_error"));
		}

		if (!(objectResult instanceof DataResponseDTO)) {
			throw new IllegalArgumentException(clazz + " doesn't extend from " + DataResponseDTO.class);
		}
		Entity entity = null;
		Set<ConstraintViolation<Object>> violations = validatorsProvider.getValidator(clazz).validate(objectResult);
		Set<String> badFields = handler.getFields();
		if (!badFields.isEmpty() || !violations.isEmpty()) {
			entity = new Entity(bc);
			for (String fieldName : badFields) {
				entity.addField(fieldName, errorMessage("error.field_deserialization_error"));
			}
			if (!ignoreBusinessErrors) {
				for (ConstraintViolation<Object> violation : violations) {
					String fieldName = null;
					for (Path.Node node : violation.getPropertyPath()) {
						fieldName = node.getName();
					}
					entity.addField(fieldName, violation.getMessage());
				}
				throw new BusinessException()
						.setEntity(entity);
			}
		}

		Set<String> fields = new HashSet<>(map.keySet());
		// штамп времени и id за поле не считаем
		fields.remove(DataResponseDTO_.vstamp.getName());
		fields.remove(DataResponseDTO_.id.getName());
		DataResponseDTO result = (DataResponseDTO) objectResult;
		result.setChangedFields(fields);
		// Чтобы можно было назад возвращать
		result.setId(bc.getId());
		if (entity != null && !entity.getFields().isEmpty()) {
			result.setErrors(entity);
		}
		return result;
	}

	public Class getDTOFromService(AnySourceBcDescription anySourceBcDescription) {
		return (Class) getResponseServiceParameters(anySourceBcDescription)[0];
	}

	private Type[] getResponseServiceParameters(Class<? extends AnySourceResponseService> cls) {
		Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(cls, AnySourceResponseService.class);
		return Stream.of(AnySourceResponseService.class.getTypeParameters())
				.map(typeArguments::get)
				.toArray(Type[]::new);
	}

	public Type[] getResponseServiceParameters(AnySourceBcDescription anySourceBcDescription) {
		return getResponseServiceParameters(getServiceClass(anySourceBcDescription));
	}

	private Class<? extends AnySourceResponseService> getServiceClass(AnySourceBcDescription anySourceBcDescription) {
		if (AnySourceResponseService.class.isAssignableFrom(anySourceBcDescription.getServiceClass())) {
			return (Class<? extends AnySourceResponseService>)anySourceBcDescription.getServiceClass();
		}
		throw new IllegalArgumentException(
				"can't cast " + anySourceBcDescription.getServiceClass() + " to " + ResponseService.class
		);
	}

	class DtoDeserializationHandler extends DeserializationProblemHandler {

		private final Set<String> fields = new HashSet<>();

		@Override
		public Object handleWeirdStringValue(
				DeserializationContext ctxt,
				Class<?> targetType,
				String valueToConvert,
				String failureMsg) {
			fields.add(ctxt.getParser().getParsingContext().getCurrentName());
			return null;
		}

		@Override
		public Object handleWeirdNumberValue(
				DeserializationContext ctxt,
				Class<?> targetType,
				Number valueToConvert,
				String failureMsg) {
			fields.add(ctxt.getParser().getParsingContext().getCurrentName());
			return null;
		}

		@Override
		public JavaType handleUnknownTypeId(
				DeserializationContext ctxt,
				JavaType baseType,
				String subTypeId,
				TypeIdResolver idResolver,
				String failureMsg) {
			fields.add(ctxt.getParser().getParsingContext().getCurrentName());
			return null;
		}

		public Set<String> getFields() {
			return fields;
		}

	}

}
