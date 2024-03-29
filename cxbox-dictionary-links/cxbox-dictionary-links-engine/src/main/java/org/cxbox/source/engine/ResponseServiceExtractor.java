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

package org.cxbox.source.engine;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.DTOUtils;
import org.cxbox.core.service.DTOMapper;
import org.cxbox.core.service.ResponseService;
import org.cxbox.model.core.entity.BaseEntity;
import java.lang.reflect.Field;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseServiceExtractor {

	private final DTOMapper dtoMapper;

	private final ApplicationContext applicationContext;

	private ResponseService<DataResponseDTO, BaseEntity> getResponseService(BusinessComponent bc) {
		if (bc.getId() == null) {
			return null;
		}
		if (!(bc.getDescription() instanceof InnerBcDescription)) {
			return null;
		}
		InnerBcDescription innerBcDescription = bc.getDescription();
		Class<? extends ResponseService> serviceClass = innerBcDescription.getServiceClass();
		return applicationContext.getBean(serviceClass);
	}

	@SneakyThrows
	public Object getFieldValue(final BusinessComponent bc, final String fieldName) {
		final ResponseService<DataResponseDTO, BaseEntity> responseService = getResponseService(bc);
		if (responseService == null) {
			return null;
		}
		final Class<DataResponseDTO> dtoClass = responseService.getTypeOfDTO();
		final Field field = FieldUtils.getField(dtoClass, fieldName, true);
		if (field == null) {
			return null;
		}
		final DtoField<DataResponseDTO, ?> dtoField = DTOUtils.getField(dtoClass, fieldName);
		if (dtoField == null) {
			return null;
		}
		Object result;
		// берем значение по возможности из кеша
		DataResponseDTO dto = responseService.getOne(bc);
		if (dto == null || !dto.isFieldComputed(fieldName)) {
			DataResponseDTO partial = dtoMapper.entityToDto(responseService.getOneAsEntity(bc), dtoClass, dtoField);
			result = field.get(partial);
			if (dto != null && !dto.isFieldComputed(fieldName)) {
				field.set(dto, result);
				dto.addComputedField(fieldName);
			}
		} else {
			result = field.get(dto);
		}
		return result;
	}

}
