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

package org.cxbox.crudma.dto;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.LocaleAware;
import org.cxbox.model.ui.entity.View;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CrudmaViewDto extends DataResponseDTO {

	private String name;

	@LocaleAware
	private String title;

	public CrudmaViewDto(final View entity) {
		this.id = entity.getId().toString();
		this.name = entity.getName();
		this.title = entity.getTitle();
	}

}
