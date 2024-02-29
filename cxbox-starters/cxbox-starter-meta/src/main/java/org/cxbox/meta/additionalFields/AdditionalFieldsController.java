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

package org.cxbox.meta.additionalFields;

import static org.cxbox.core.config.properties.APIProperties.CXBOX_API_PATH_SPEL;

import java.util.List;
import lombok.AllArgsConstructor;
import org.cxbox.core.config.cache.CxboxCachingService;
import org.cxbox.core.dto.RequestDTO;
import org.cxbox.core.dto.ResponseDTO;
import org.cxbox.core.util.ResponseBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping(CXBOX_API_PATH_SPEL + "/personalAdditionalFields")
public class AdditionalFieldsController {

	private final AdditionalFieldsService additionalFieldsService;

	private final CxboxCachingService cxboxCachingService;

	@RequestMapping(method = RequestMethod.POST)
	public ResponseDTO createAdditionalFields(@RequestBody RequestDTO<List<AdditionalFieldsDTO>> requestDTO) {
		List<AdditionalFieldsDTO> additionalFieldsListResponse =
				additionalFieldsService.createAdditionalFields(requestDTO.getData());
		cxboxCachingService.evictUserCache(); //TODO>>REMOVE ONLY CURRENT USER CACHE!!!!
		return ResponseBuilder.build(additionalFieldsListResponse);
	}

	@RequestMapping(method = RequestMethod.DELETE)
	public void deleteAdditionalFields(@RequestParam RequestDTO<List<Long>> ids) {
		additionalFieldsService.deleteAdditionalFields(ids.getData());
		cxboxCachingService.evictUserCache(); //TODO>>REMOVE ONLY CURRENT USER CACHE!!!!
	}

	@RequestMapping(method = RequestMethod.PUT)
	public ResponseDTO updateAdditionalFields(@RequestBody RequestDTO<List<AdditionalFieldsDTO>> requestDTO) {
		List<AdditionalFieldsDTO> additionalFieldsListResponse =
				additionalFieldsService.updateAdditionalFields(requestDTO.getData());
		cxboxCachingService.evictUserCache(); //TODO>>REMOVE ONLY CURRENT USER CACHE!!!!
		return ResponseBuilder.build(additionalFieldsListResponse);
	}

}
