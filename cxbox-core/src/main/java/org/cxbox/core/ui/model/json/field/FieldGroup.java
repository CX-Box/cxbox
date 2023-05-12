
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

import org.cxbox.core.ui.model.json.field.FieldMeta.FieldContainer;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class FieldGroup implements FieldContainer {

	@JsonProperty(value = "break")
	private Boolean breakBlock;

	private Boolean fullSize;

	private String blockId;

	private String name;

	private Boolean visible;

	private Boolean newRow;

	@JsonProperty("fields")
	private List<FieldMeta> children;

}
