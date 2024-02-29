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

package org.cxbox.meta.data;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cxbox.api.data.dto.LocaleAware;
import org.cxbox.api.util.jackson.deser.convert.Raw2StringDeserializer;
import org.cxbox.core.util.filter.SearchParameter;

@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class ViewDTO implements Serializable {

	private Long id;

	@SearchParameter
	private String name;

	private String template;

	@LocaleAware
	@SearchParameter
	private String title;

	@SearchParameter
	private String url;

	private Boolean customizable;

	private Boolean editable;

	private List<WidgetDTO> widgets;

	private Integer columns;

	private Integer rowHeight;

	private Boolean readOnly;

	private Boolean ignoreHistory;

	@JsonRawValue
	@JsonDeserialize(using = Raw2StringDeserializer.class)
	private String options;



}
