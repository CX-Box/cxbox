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

package org.cxbox.meta.metahotreload.service;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.cxbox.meta.metahotreload.dto.BcSourceDTO;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.metahotreload.util.JsonUtils;
import org.cxbox.meta.entity.Bc;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BcUtil {

	private final MetaRepository metaRepository;

	@Qualifier("cxboxObjectMapper")
	private final ObjectMapper objectMapper;

	public void process(@NonNull List<BcSourceDTO> dtos) {
		dtos.stream().map(bcDto -> mapToEntity(bcDto, objectMapper)).forEach(metaRepository::saveBc);
	}

	@NonNull
	private static Bc mapToEntity(@NonNull BcSourceDTO bcDto, ObjectMapper objectMapper) {
		return new Bc()
				.setName(bcDto.getName())
				.setParentName(bcDto.getParentName())
				.setQuery(bcDto.getQuery())
				.setDefaultOrder(bcDto.getDefaultOrder())
				.setReportDateField(bcDto.getReportDateField())
				.setPageLimit(bcDto.getPageLimit())
				.setEditable(ofNullable(bcDto.getEditable()).map(val -> val > 0).orElse(false))
				.setRefresh(ofNullable(bcDto.getRefresh()).map(val -> val > 0).orElse(false))
				.setBinds(JsonUtils.serializeOrElseNull(objectMapper, bcDto.getBinds()));
	}
}
