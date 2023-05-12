
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

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.model.ui.entity.FilterGroup;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterGroupDTO extends DataResponseDTO {

	private String name;

	private String filters;

	private String bc;

	@Builder
	public FilterGroupDTO(FilterGroup entity) {
		this.id = entity.getId().toString();
		this.name = entity.getName();
		this.filters = entity.getFilters();
		this.bc = entity.getBc();
	}

}
