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

package org.cxbox.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.core.dto.rowmeta.FilterGroupDTO;
import org.cxbox.core.service.spec.FilterGroupService;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.ui.entity.FilterGroup;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FilterGroupServiceImpl implements FilterGroupService {

	private final JpaDao jpaDao;

	private final SessionService service;

	private final TransactionService transactionService;


	@Override
	public List<FilterGroupDTO> create(List<FilterGroupDTO> filterGroupDTOList) {

		List<FilterGroupDTO> filterGroupsDTO = new ArrayList<>();

		transactionService.invokeInTx(() -> {
			filterGroupDTOList.forEach(fgDTO -> {
				Long id = jpaDao.save(filterGroupFromDTO(fgDTO)
						.setUser(service.getSessionUser()));
				fgDTO.setId(id.toString());
				filterGroupsDTO.add(fgDTO);
			});
			return null;
		});
		return filterGroupsDTO;
	}

	@Override
	public void delete(List<Long> ids) {
		transactionService.invokeInTx(() -> {
			ids.forEach(id -> jpaDao.delete(FilterGroup.class, id));
			return null;
		});

	}

	private FilterGroup filterGroupFromDTO(FilterGroupDTO filterGroupDTO) {
		return new FilterGroup()
				.setFilters(filterGroupDTO.getFilters())
				.setName(filterGroupDTO.getName())
				.setBc(filterGroupDTO.getBc());
	}

}
