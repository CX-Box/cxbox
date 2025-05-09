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

import java.io.Serializable;
import lombok.experimental.Accessors;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.LocaleAware;
import org.cxbox.api.util.jackson.deser.convert.Raw2StringDeserializer;
import org.cxbox.api.util.jackson.ser.contextaware.I18NAwareRawStringSerializer;
import org.cxbox.core.util.filter.SearchParameter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.meta.additionalFields.AdditionalFieldsDTO;

@Slf4j
@NoArgsConstructor
@Getter
@Setter
@JsonFilter("")
@Accessors(chain = true)
public class WidgetDTO extends DataResponseDTO implements BcSource, Serializable {




	@SearchParameter
	private String name;

	private Integer widgetId;

	private Long position;

	private String descriptionTitle;

	private String description;

	private String snippet;

	private Boolean showExportStamp;

	private Long limit;

	private String type;

	private String url;

	private AdditionalFieldsDTO personalFields;

	@SearchParameter(name = "bc")
	private String bcName;

	@LocaleAware
	@SearchParameter
	private String title;

	@JsonRawValue
	@JsonSerialize(using = I18NAwareRawStringSerializer.class)
	@JsonDeserialize(using = Raw2StringDeserializer.class)
	private String fields;

	@JsonRawValue
	@JsonSerialize(using = I18NAwareRawStringSerializer.class)
	@JsonDeserialize(using = Raw2StringDeserializer.class)
	private String options;

	@JsonRawValue
	@JsonSerialize(using = I18NAwareRawStringSerializer.class)
	@JsonDeserialize(using = Raw2StringDeserializer.class)
	private String pivotFields;

	@JsonRawValue
	@JsonSerialize(using = I18NAwareRawStringSerializer.class)
	@JsonDeserialize(using = Raw2StringDeserializer.class)
	private String axisFields;

	@JsonRawValue
	@JsonDeserialize(using = Raw2StringDeserializer.class)
	private String showCondition;

	@JsonRawValue
	@JsonSerialize(using = I18NAwareRawStringSerializer.class)
	@JsonDeserialize(using = Raw2StringDeserializer.class)
	private String chart;

	@JsonRawValue
	@JsonSerialize(using = I18NAwareRawStringSerializer.class)
	@JsonDeserialize(using = Raw2StringDeserializer.class)
	private String graph;

	private Number x;

	private Number y;

	private Number width;

	private Number height;

	private Number minHeight;

	private Number maxHeight;

	private Number minWidth;

	private Number maxWidth;

	private Boolean isDraggable;

	private Boolean isResizable;

	private Long gridWidth;

	private Long gridBreak;

	private Boolean hide;




}
