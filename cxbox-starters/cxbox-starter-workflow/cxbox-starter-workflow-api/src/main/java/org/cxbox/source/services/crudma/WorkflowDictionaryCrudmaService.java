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

package org.cxbox.source.services.crudma;

import org.cxbox.WorkflowServiceAssociation;
import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.api.data.dictionary.DictionaryType;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.AbstractCrudmaService;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.util.ListPaging;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowDictionaryCrudmaService extends AbstractCrudmaService {

	private final List<FieldDTO> FIELD_DTO_LIST = ImmutableList.<FieldDTO>builder()
			.add(FieldDTO.disabledFilterableField(DictionaryDto_.key))
			.add(FieldDTO.disabledFilterableField(DictionaryDto_.value))
			.add(FieldDTO.disabledFilterableField(DictionaryDto_.description))
			.build();

	private final DictionaryCache dictionaryCache;

	private Collection<SimpleDictionary> getDictionaries(BusinessComponent bc) {
		if (WorkflowServiceAssociation.wfStepType.isBc(bc)) {
			return dictionaryCache.getAll(DictionaryType.TASK_STATUS);
		}
		return Collections.emptyList();
	}

	@Override
	public ResultPage<? extends DataResponseDTO> getAll(BusinessComponent bc) {
		final List<DictionaryDto> dictionaries = getDictionaries(bc).stream()
				.map(DictionaryDto::new)
				.collect(Collectors.toList());
		return ListPaging.getResultPage(dictionaries, bc.getParameters());
	}

	@Override
	public long count(BusinessComponent bc) {
		return getDictionaries(bc).size();
	}

	@Override
	public MetaDTO getMeta(BusinessComponent bc) {
		return buildMeta(FIELD_DTO_LIST);
	}

	@Override
	public MetaDTO getMetaEmpty(BusinessComponent bc) {
		return buildMeta(Collections.emptyList());
	}

	@Getter
	@Setter
	@NoArgsConstructor
	final class DictionaryDto extends DataResponseDTO {

		private String key;

		private String value;

		private String description;

		DictionaryDto(SimpleDictionary dict) {
			this.id = dict.getKey();
			this.key = dict.getKey();
			this.value = dict.getValue();
			this.description = dict.getDescription();
		}

	}

}
