/*-
 * #%L
 * IO Cxbox - Source
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

package org.cxbox.crudma.dto;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.LocaleAware;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.model.ui.entity.View;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViewsDTO extends DataResponseDTO {

	@SearchParameter
	private String name;

	@LocaleAware
	@SearchParameter
	private String title;

	private String url;

	@SearchParameter
	private String path;

	public ViewsDTO(View view) {
		this.id = view.getId().toString();
		this.name = view.getName();
		this.title = view.getTitle();
		this.url = view.getUrl();
	}

}
