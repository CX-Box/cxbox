/*-
 * #%L
 * IO Cxbox - Dictionary Links Implementation
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

package org.cxbox.source.service.data.impl;

import static org.cxbox.core.controller.param.SearchOperation.CONTAINS;
import static org.cxbox.core.controller.param.SearchOperation.EQUALS;

import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.core.controller.param.FilterParameter;
import org.cxbox.core.controller.param.FilterParameters;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.controller.param.SearchOperation;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.AbstractResponseService;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRule;
import org.cxbox.model.dictionary.links.entity.DictionaryLnkRuleValue;
import org.cxbox.source.dto.DictionaryLnkRuleValueDto;
import org.cxbox.source.service.data.DictionaryLnkRuleValueAssocService;
import org.cxbox.source.service.meta.DictionaryLnkRuleValueFieldMetaBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DictionaryLnkRuleValueAssocServiceImpl extends
		AbstractResponseService<DictionaryLnkRuleValueDto, DictionaryLnkRuleValue> implements
		DictionaryLnkRuleValueAssocService {

	@Autowired
	private DictionaryCache dictionaryCache;

	public DictionaryLnkRuleValueAssocServiceImpl() {
		super(
				DictionaryLnkRuleValueDto.class,
				DictionaryLnkRuleValue.class,
				null,
				DictionaryLnkRuleValueFieldMetaBuilder.class
		);
	}

	@Override
	public ResultPage<DictionaryLnkRuleValueDto> getList(BusinessComponent bc) {
		QueryParameters params = bc.getParameters();
		DictionaryLnkRule parent = baseDAO.findById(DictionaryLnkRule.class, bc.getParentIdAsLong());
		List<DictionaryLnkRuleValueDto> result = dictionaryCache
				.getAll(parent.getType()).stream()
				.map(dictDTO -> {
					DictionaryLnkRuleValueDto dto = new DictionaryLnkRuleValueDto();
					dto.setId(dictDTO.getKey());
					dto.setValueCd(dictDTO.getKey());
					dto.setValue(dictDTO.getValue());
					return dto;
				})
				.filter(dto -> filterByQueryParams(dto, params.getFilter()))
				.skip(params.getPageNumber() * params.getPageSize())
				.limit(params.getPageSize() + 1)
				.collect(Collectors.toList());
		return dtoListToResultPage(result, params.getPageSize());
	}

	private boolean filterByQueryParams(DictionaryLnkRuleValueDto dto, FilterParameters searchParameters) {
		String dtoValue = dto.getValue().toLowerCase();
		String dtoValueCd = dto.getValueCd().toLowerCase();
		for (FilterParameter parameter : searchParameters) {
			if (Objects.isNull(parameter.getStringValue())) {
				return true;
			}
			String value = parameter.getStringValue().toLowerCase();
			SearchOperation operation = parameter.getOperation();
			String fieldName = parameter.getName();
			if (operation == EQUALS) {
				if ("value".equals(fieldName)) {
					return Objects.equals(value, dtoValue);
				}
				if ("valueCd".equals(fieldName)) {
					return Objects.equals(value, dtoValueCd);
				}
			}
			if (operation == CONTAINS) {
				if ("value".equals(fieldName)) {
					return dtoValue.contains(value);
				}
				if ("valueCd".equals(fieldName)) {
					return dtoValueCd.contains(value);
				}
			}
		}
		return true;
	}

	@Override
	public long count(BusinessComponent bc) {
		DictionaryLnkRule parent = baseDAO.findById(DictionaryLnkRule.class, bc.getParentIdAsLong());
		return dictionaryCache.getAll(parent.getType()).size();
	}

}
