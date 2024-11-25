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

package org.cxbox.dto;

import java.io.Serializable;
import lombok.experimental.Accessors;
import org.cxbox.api.data.dto.LocaleAware;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Accessors(chain = true)
public class ScreenResponsibility implements Serializable {

	public static final TypeReference<List<ScreenResponsibility>> LIST_TYPE_REFERENCE = new ListTypeReference();

	private String id;

	private String name;

	//not used directly - used only for sorting on backend side
	private Integer order;

	@LocaleAware
	private String text;

	private String url;

	private String icon;

	@Deprecated(since = "4.0.0-M12")
	private Boolean defaultScreen;

	//ScreenDTO.class
	private Object meta;

	public static class ListTypeReference extends TypeReference<List<ScreenResponsibility>> {

	}

}
