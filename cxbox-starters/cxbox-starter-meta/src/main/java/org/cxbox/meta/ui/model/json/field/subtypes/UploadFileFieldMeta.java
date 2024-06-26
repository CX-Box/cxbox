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

package org.cxbox.meta.ui.model.json.field.subtypes;

import org.cxbox.meta.ui.field.CxboxWidgetField;
import org.cxbox.meta.ui.field.link.LinkToField;
import org.cxbox.meta.ui.model.json.field.FieldMeta.FieldMetaBase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CxboxWidgetField({"fileUpload", "printForm"})
public class UploadFileFieldMeta extends FieldMetaBase {

	@LinkToField
	private String fileIdKey;

	private String fileSource;

	private Preview preview;

	@Getter
	@Setter
	public static class Preview {

		private Boolean enabled;

		@LinkToField
		private String titleKey;

		@LinkToField
		private String hintKey;

		private String mode; //'popup' | 'side-panel'

		private Boolean miniPreview;

	}

}
