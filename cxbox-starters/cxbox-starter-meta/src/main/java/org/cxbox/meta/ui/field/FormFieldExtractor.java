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

package org.cxbox.meta.ui.field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cxbox.api.util.i18n.LocalizationFormatter;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.meta.ui.field.link.LinkFieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.json.field.FieldGroup;
import org.cxbox.meta.ui.model.json.field.FieldMeta;
import org.cxbox.meta.entity.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class FormFieldExtractor extends BaseFieldExtractor {

	public FormFieldExtractor(@Autowired LinkFieldExtractor linkFieldExtractor) {
		super(linkFieldExtractor);
	}

	@Override
	public Set<BcField> extract(final Widget widget) {
		final Set<BcField> widgetFields = new HashSet<>(extractFieldsFromTitle(widget, LocalizationFormatter.i18n(widget.getTitle())));
		for (final FieldGroup group : JsonUtils.readValue(FieldGroup[].class, widget.getFields())) {
			if (group.getChildren() != null) {
				for (final FieldMeta field : group.getChildren()) {
					widgetFields.addAll(extract(widget, field));
				}
			}
		}
		return widgetFields;
	}

	@Override
	public List<String> getSupportedTypes() {
		List<String> result = new ArrayList<>();
		result.add("Info");
		result.add("InfoFloat");
		result.add("Form");
		return result;
	}

}
