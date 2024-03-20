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

	/**
	 * "fileAccept format - see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Attributes/accept">...</a>".
	 *  Null means all file formats allowed
	 */
	private String fileAccept;

	/**
	 * same as fileAccept, but value is taken from DTO field (e.g. can be changed dynamically accordingly to your backend logic)
	 */
	private String fileAcceptKey;

	private String fileSource;

}
