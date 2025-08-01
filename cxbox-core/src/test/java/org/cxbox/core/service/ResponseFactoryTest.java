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

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.config.JacksonConfig;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.service.impl.ValidatorsProviderImpl;
import org.cxbox.core.test.util.TestResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.cxbox.core.dto.BusinessError.Entity;
import java.util.LinkedHashMap;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DirtiesContext
@SpringJUnitConfig({
		ResponseFactory.class,
		ValidatorsProviderImpl.class,
		JacksonConfig.class,
		ChangedNowValidationService.class
})
class ResponseFactoryTest {

	@InjectMocks
	@Autowired
	private ResponseFactory responseFactory;

	private LinkedHashMap<String, Object> map;
	private BusinessComponent bc;


	@BeforeEach
	void setup() {
		map = new LinkedHashMap<>();
		bc = mock(BusinessComponent.class);
	}

	@Test
	void getDTOFromMapIgnoreBusinessErrors() {
		map.put("validatedField", "true");
		responseFactory.getDTOFromMapIgnoreBusinessErrors(map, TestResponseDto.class, bc);
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			responseFactory.getDTOFromMap(map, TestResponseDto.class, bc);
		});
		assertThat(exception.getEntity().getFields().get("validatedField")).isEqualTo("Minimal length is 10");
	}

	@Test
	void fieldDeserializationErrors() {
		map.put("number", "aaaa");
		DataResponseDTO result = responseFactory.getDTOFromMapIgnoreBusinessErrors(map, TestResponseDto.class, bc);
		assertThat(result.getErrors()).isNotNull();
		assertThat(((Entity)result.getErrors()).getFields().get("number"))
			.isEqualTo(errorMessage("error.field_deserialization_error"));
	}

	@Test
	void classDeserializationError() {
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			responseFactory.getDTOFromMapIgnoreBusinessErrors(map, String.class, bc);
		});
		assertThat(exception.getPopup().get(0))
				.isEqualTo(errorMessage("error.dto_deserialization_error"));
	}
}
