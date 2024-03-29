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

import org.cxbox.api.data.dto.LocaleAware;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ScreenResponsibility {

	public static final TypeReference<List<ScreenResponsibility>> LIST_TYPE_REFERENCE = new ListTypeReference();

	private String id;

	private String name;

	@LocaleAware
	private String text;

	private String url;

	private String icon;

	private boolean defaultScreen;

	//ScreenDTO.class
	private Object meta;

	public static class ListTypeReference extends TypeReference<List<ScreenResponsibility>> {

	}

}
