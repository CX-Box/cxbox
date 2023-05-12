
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

package org.cxbox.source.dto;

import static org.cxbox.api.data.dictionary.DictionaryCache.dictionary;

import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.api.data.dictionary.DictionaryType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.dto.DrillDownType;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.model.dictionary.links.entity.CustomizableResponseService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomizableResponseServiceDto extends DataResponseDTO {

	@SearchParameter
	private String serviceName;

	private String docName;

	private String docUrl;

	private String docUrlType;

	public CustomizableResponseServiceDto(CustomizableResponseService entity) {
		this.id = entity.getId().toString();
		this.serviceName = entity.getServiceName();
		DictionaryCache dictionary = dictionary();
		if (dictionary.containsKey(serviceName, DictionaryType.BUSINESS_SERVICE_NAME)) {
			this.docName = DictionaryType.BUSINESS_SERVICE_NAME.lookupValue(new LOV(this.serviceName));
		}
		if (dictionary.containsKey(serviceName, DictionaryType.BUSINESS_SERVICE_URL)) {
			this.docUrl = DictionaryType.BUSINESS_SERVICE_URL.lookupValue(new LOV(this.serviceName));
		}
		this.docUrlType = DrillDownType.EXTERNAL_NEW.getValue();
	}

}
