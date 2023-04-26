/*-
 * #%L
 * IO Cxbox - Workflow API
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.source.services.crudma;

import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.AbstractCrudmaService;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.util.ListPaging;
import org.cxbox.source.dto.DmnTaskFieldsDto;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DmnHelperFieldsCrudmaService extends AbstractCrudmaService {

	private static final List<DmnTaskFieldsDto> HELPER_FIELDS = ImmutableList.<DmnTaskFieldsDto>builder()
			.add(new DmnTaskFieldsDto("1", "Сегодняшний день", "helper.today", "date"))
			.build();

	private static final List<FieldDTO> FIELD_DTO_LIST = ImmutableList.<FieldDTO>builder()
			.add(FieldDTO.disabledFilterableField("id"))
			.add(FieldDTO.disabledFilterableField("title"))
			.add(FieldDTO.disabledFilterableField("key"))
			.add(FieldDTO.disabledFilterableField("type"))
			.build();

	@Override
	public ResultPage<? extends DataResponseDTO> getAll(BusinessComponent bc) {
		return ListPaging.getResultPage(HELPER_FIELDS, bc.getParameters());
	}

	@Override
	public long count(BusinessComponent bc) {
		return HELPER_FIELDS.size();
	}

	@Override
	public MetaDTO getMeta(BusinessComponent bc) {
		return buildMeta(FIELD_DTO_LIST);
	}

	@Override
	public MetaDTO getMetaEmpty(BusinessComponent bc) {
		return buildMeta(Collections.emptyList());
	}

}
