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

package org.cxbox.core.dto.rowmeta;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.Getter;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.dictionary.DictionaryProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

class RowDependentFieldsMetaTest {

	@Mock
	ObjectMapper objectMapper;

	@Mock
	Optional<DictionaryProvider> dictionaryProvider;


	@InjectMocks
	RowDependentFieldsMeta<TestDTO> rowDependentFieldsMeta;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testSetPlaceholder() {
		rowDependentFieldsMeta = new RowDependentFieldsMeta<>(objectMapper, dictionaryProvider);
		FieldDTO field = FieldDTO.enabledField("test");
		DtoField<TestDTO, String> test = new DtoField<>("test", TestDTO::getTest, String.class);
		assertThat(field.getPlaceholder()).isNull();
		rowDependentFieldsMeta.add(field);
		rowDependentFieldsMeta.setPlaceholder(test, "placeholder");
		assertThat(field.getPlaceholder()).isEqualTo("placeholder");
	}

	@Getter
	public static class TestDTO extends DataResponseDTO {
		private String test;
	}
}
