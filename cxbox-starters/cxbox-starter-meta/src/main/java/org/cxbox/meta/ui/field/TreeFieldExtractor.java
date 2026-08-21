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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cxbox.api.util.i18n.LocalizationFormatter;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.field.link.LinkFieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;
import org.cxbox.meta.ui.model.json.WidgetOptions;
import org.cxbox.meta.ui.model.json.field.FieldMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component

public class TreeFieldExtractor extends BaseFieldExtractor {

	private final LinkFieldExtractor linkFieldExtractor;

	public TreeFieldExtractor(@Autowired LinkFieldExtractor linkFieldExtractor, LinkFieldExtractor linkFieldExtractor1) {
		super(linkFieldExtractor);
		this.linkFieldExtractor = linkFieldExtractor1;
	}

	@Override
	public Set<BcField> extract(final WidgetDTO widget) {
		final Set<BcField> widgetFields = new HashSet<>(extractFieldsFromTitle(
				widget,
				LocalizationFormatter.i18n(widget.getTitle())
		));
		for (final FieldMeta field : JsonUtils.readValue(FieldMeta[].class, widget.getFields())) {
			widgetFields.addAll(extract(widget, field));
		}

		widgetFields.addAll(extractFieldsFromOptions(widget));
		return widgetFields;
	}


	private Set<BcField> extractFieldsFromOptions(final WidgetDTO widget) {
		WidgetOptions options = linkFieldExtractor.extractWidgetOptions(widget);
		if (options == null || options.getTree() == null) {
			return Collections.emptySet();
		}

		Set<BcField> treeFields = linkFieldExtractor.extract(
				widget.getName(), widget.getBcName(), options.getTree()
		);

		Set<BcField> result = new HashSet<>(treeFields);

		if (!treeFields.contains("parentFieldKey")) {
			result.add(createField(widget, "parentId"));
		}

		if (!treeFields.contains("isLeafFieldKey")) {
			result.add(createField(widget, "isLeaf"));
		}

		return result;
	}

	private BcField createField(WidgetDTO widget, String fieldName) {
		return new BcField(widget.getBcName(), fieldName)
				.putAttribute(Attribute.WIDGET_NAME, widget.getName());
	}

	@Override
	public List<String> getSupportedTypes() {
		List<String> result = new ArrayList<>();
		result.add("Tree");
		return result;
	}

	@Override
	public int getPriority() {
		return 1;
	}

}
