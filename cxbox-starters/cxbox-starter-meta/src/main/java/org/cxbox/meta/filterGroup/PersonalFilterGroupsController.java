/*
 * © OOO "SI IKS LAB", 2022-2024
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

package org.cxbox.meta.filterGroup;


import static org.cxbox.core.config.properties.APIProperties.CXBOX_API_PATH_SPEL;

import java.util.List;
import lombok.AllArgsConstructor;
import org.cxbox.core.config.cache.CxboxCachingService;
import org.cxbox.core.dto.RequestDTO;
import org.cxbox.core.dto.ResponseDTO;
import org.cxbox.core.util.ResponseBuilder;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.meta.data.FilterGroupDTO;
import org.cxbox.meta.data.FilterGroupDTOList;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CXBOX_API_PATH_SPEL + "/personalFilterGroups")
@AllArgsConstructor
public class PersonalFilterGroupsController {

	private final PersonalFilterGroupService personalFilterGroupService;

	private final CxboxCachingService cxboxCachingService;

	private final SessionService sessionService;

	@PostMapping
	public ResponseDTO createFilterGroup(@RequestBody RequestDTO<FilterGroupDTOList> request) {
		List<FilterGroupDTO> filterGroupDTOResponse = personalFilterGroupService
				.create(request.getData().getFilterGroups());
		cxboxCachingService.evictUserCache(); //TODO>>REMOVE ONLY CURRENT USER CACHE!!!!
		return ResponseBuilder.build(filterGroupDTOResponse);
	}

	@DeleteMapping
	public void deleteFilterGroup(@RequestBody List<Long> ids) {
		personalFilterGroupService.delete(ids);
		cxboxCachingService.evictUserCache(); //TODO>>REMOVE ONLY CURRENT USER CACHE!!!!
	}

}