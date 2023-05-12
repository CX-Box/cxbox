
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

package org.cxbox.core.metahotreload.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cxbox.core.metahotreload.dto.BcSourceDTO;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.ui.entity.Bc;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import static org.cxbox.core.metahotreload.util.JsonUtils.serializeOrElseNull;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class BcUtil {

	private final JpaDao jpaDao;

	@Qualifier("cxboxObjectMapper")
	private final ObjectMapper objectMapper;

	public void process(@NonNull List<BcSourceDTO> dtos) {
		dtos.stream().map(bcDto -> mapToEntity(bcDto, objectMapper)).forEach(jpaDao::save);
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
				.setBinds(serializeOrElseNull(objectMapper, bcDto.getBinds()));
	}
}
