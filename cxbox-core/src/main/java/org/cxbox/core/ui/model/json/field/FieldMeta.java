
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

package org.cxbox.core.ui.model.json.field;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.cxbox.core.ui.field.PackageScanFieldIdResolver;
import org.cxbox.core.ui.field.link.LinkToField;
import org.cxbox.core.ui.model.json.CellStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type", defaultImpl = FieldMeta.ListColumnGroupMeta.class, visible = true)
@JsonTypeIdResolver(PackageScanFieldIdResolver.class)
public abstract class FieldMeta extends CellStyle {

	private String key;

	private String title;

	public interface FieldContainer {

		List<FieldMeta> getChildren();

	}

	@Getter
	@Setter
	public static class ListColumnGroupMeta extends FieldMeta implements FieldContainer {

		@JsonProperty("childrens")
		private List<FieldMeta> children;

	}


	@Getter
	@Setter
	public abstract static class FieldMetaBase extends FieldMeta {

		private String type;

		private Boolean isValue;

		private Boolean isCol;

		private Boolean isRow;

		private String label;

		private Boolean xDefault;

		private Boolean yDefault;

		private Boolean fixedAxis;

		private Boolean required;

		private Boolean permanent;

		private Boolean drillDown;

		@LinkToField
		private String drillDownKey;

		@LinkToField
		private String drillDownTypeKey;

		private String bgColor;

		@LinkToField
		private String bgColorKey;

		@LinkToField
		private String iconParamsKey;

		private String iconType;

		private String iconColor;

		@LinkToField
		private String iconTypeKey;

		@LinkToField
		private String iconColorKey;

		private String groupName;

		private Long width;

		private String hintTitle;

		private String hintText;

		@LinkToField
		private String hintKey;

		private Long maxInput;

		private List<MultiSourceInfo> multisource;

		private Map<String, String> customFields = new HashMap<>();

		private String filterBy;

		@JsonAnySetter
		public void setCustomFields(String name, String value) {
			customFields.put(name, value);
		}

		@Getter
		@Setter
		public static class MultiSourceInfo {

			private String key;

			private String label;

		}

	}

}
