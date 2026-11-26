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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.cxbox.api.config.CxboxBeanProperties;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;

@DirtiesContext
@SpringJUnitConfig({
		ResponseFactory.class,
		AnySourceResponseFactory.class,
		ValidatorsProviderImpl.class,
		JacksonConfig.class,
		ChangedNowValidationService.class
})
class ResponseFactoryTest {

	@InjectMocks
	@Autowired
	private ResponseFactory responseFactory;

	@Autowired
	private AnySourceResponseFactory anySourceResponseFactory;

	@Autowired
	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private ObjectMapper mapper;

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

	@Test
	void eachReadReportsOnlyItsOwnFieldErrors() {
		Map<String, Object> wrong = Map.of("number", "invalid", "string", "valid",
				"changedNow_", Map.of("bigDecimal", "invalid"));
		Map<String, Object> valid = Map.of("number", 12);

		for (DataResponseDTO record : List.of(
				responseFactory.getDTOFromMapIgnoreBusinessErrors(wrong, TestResponseDto.class, bc),
				anySourceResponseFactory.getDTOFromMapIgnoreBusinessErrors(wrong, TestResponseDto.class, bc))) {
			String wrongFormat = errorMessage("error.field_deserialization_error");
			assertThat(((Entity) record.getErrors()).getFields()).containsOnly(entry("number", wrongFormat));
			assertThat(((TestResponseDto) record).getString()).isEqualTo("valid");
			var changedNow = record.getChangedNowParam().getChangedNowDTO();
			assertThat(((Entity) changedNow.getErrors()).getFields()).containsOnly(entry("bigDecimal", wrongFormat));
		}
		assertThat(responseFactory.getDTOFromMapIgnoreBusinessErrors(valid, TestResponseDto.class, bc).getErrors()).isNull();
		assertThat(anySourceResponseFactory.getDTOFromMapIgnoreBusinessErrors(valid, TestResponseDto.class, bc).getErrors())
				.isNull();
	}

	@Test
	void readsDoNotPileUpHandlersInTheSharedMapper() {
		for (int i = 0; i < 10; i++) {
			responseFactory.getDTOFromMapIgnoreBusinessErrors(Map.of("number", "invalid"), TestResponseDto.class, bc);
			anySourceResponseFactory.getDTOFromMapIgnoreBusinessErrors(Map.of("number", 12), TestResponseDto.class, bc);
		}
		int handlers = 0;
		for (var node = mapper.getDeserializationConfig().getProblemHandlers(); node != null; node = node.next()) {
			handlers++;
		}
		assertThat(handlers).isEqualTo(1);
	}

	@Test
	void otherReadsOfTheSharedMapperKeepJacksonErrors() {
		responseFactory.getDTOFromMapIgnoreBusinessErrors(Map.of("number", "invalid"), TestResponseDto.class, bc);

		assertThrows(IllegalArgumentException.class,
				() -> mapper.convertValue(Map.of("number", "invalid"), TestResponseDto.class));
	}

	@Test
	void concurrentReadsGetOnlyTheirOwnFieldErrors() throws Exception {
		List<String> fields = List.of("number", "bigDecimal", "dateTime", "");
		ExecutorService pool = Executors.newFixedThreadPool(fields.size());
		try {
			List<Future<Set<String>>> results = new ArrayList<>();
			for (String field : fields) {
				results.add(pool.submit(() -> {
					Set<String> errorFields = new HashSet<>();
					for (int i = 0; i < 500; i++) {
						Map<String, Object> data = field.isEmpty() ? Map.of("number", 12) : Map.of(field, "invalid");
						var record = i % 2 == 0
								? responseFactory.getDTOFromMapIgnoreBusinessErrors(data, TestResponseDto.class, bc)
								: anySourceResponseFactory.getDTOFromMapIgnoreBusinessErrors(data, TestResponseDto.class, bc);
						if (record.getErrors() != null) {
							errorFields.addAll(((Entity) record.getErrors()).getFields().keySet());
						} else if (!field.isEmpty()) {
							errorFields.add("lost");
						}
					}
					return errorFields;
				}));
			}
			for (int i = 0; i < fields.size(); i++) {
				String field = fields.get(i);
				assertThat(results.get(i).get(1, TimeUnit.MINUTES))
						.isEqualTo(field.isEmpty() ? Set.of() : Set.of(field));
			}
		} finally {
			pool.shutdownNow();
		}
	}
}
