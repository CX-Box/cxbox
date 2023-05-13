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

package org.cxbox.core.dto.data.view;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.LocaleAware;
import org.cxbox.api.util.jackson.deser.convert.Raw2StringDeserializer;
import org.cxbox.core.util.filter.SearchParameter;
import org.cxbox.model.ui.entity.Screen;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonFilter("")
public class ScreenDTO extends DataResponseDTO {

	@SearchParameter
	private String name;

	@LocaleAware
	@SearchParameter
	private String title;

	private String primary;

	private List<ViewDTO> views;

	private BusinessObjectDTO bo;

	private ScreenNavigation navigation;

	@JsonRawValue
	@JsonDeserialize(using = Raw2StringDeserializer.class)
	private String primaries; // TODo Выпилить после показа CBR-1488

	public ScreenDTO(Screen screen) {
		this.id = screen.getId().toString();
		this.name = screen.getName();
		this.title = screen.getTitle();
		this.primary = screen.getPrimary();
		this.primaries = screen.getPrimaries();
	}

}
