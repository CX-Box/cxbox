/*
 * © OOO "SI IKS LAB", 2022-2024
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.field.link.LinkFieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.json.field.FieldGroup;
import org.cxbox.meta.ui.model.json.field.FieldMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InfoFieldExtractor extends BaseFieldExtractor {

	public InfoFieldExtractor(@Autowired LinkFieldExtractor linkFieldExtractor) {
		super(linkFieldExtractor);
	}

	@Override
	public Set<BcField> extract(final WidgetDTO widget) {
		final Set<BcField> widgetFields = new HashSet<>(extractFieldsFromTitle(widget, widget.getTitle()));
		FieldMeta[] fieldMetas = JsonUtils.readValue(FieldMeta[].class, widget.getFields());
		FieldGroup[] fieldGroups = JsonUtils.readValue(FieldGroup[].class, widget.getFields());
		for (final FieldMeta field : fieldMetas) {
			if (field.getKey() != null) {
				widgetFields.addAll(extract(widget, field));
			}
		}
		for (final FieldGroup group : fieldGroups) {
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
		return List.of(
				"Info"
		);
	}

	@Override
	public int getPriority() {
		return 1;
	}

}
