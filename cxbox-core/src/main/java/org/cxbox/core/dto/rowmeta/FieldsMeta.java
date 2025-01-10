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
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.dictionary.DictionaryProvider;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class FieldsMeta<T extends DataResponseDTO> extends FieldsGroupingHierarchyMeta<T> {

	public FieldsMeta(@Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objectMapper, Optional<DictionaryProvider> dictionaryProvider) {
		super(objectMapper, dictionaryProvider);
	}

}
