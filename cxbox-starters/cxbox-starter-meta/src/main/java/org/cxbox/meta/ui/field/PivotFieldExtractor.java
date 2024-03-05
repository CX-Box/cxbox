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
import lombok.RequiredArgsConstructor;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.field.link.LinkFieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;
import org.cxbox.meta.ui.model.json.PivotMeta;
import org.cxbox.meta.ui.model.json.PivotMeta.TableColRow;
import org.cxbox.meta.ui.model.json.PivotMeta.TableValue;
import org.cxbox.meta.ui.model.json.field.FieldMeta;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PivotFieldExtractor implements FieldExtractor {

	private final LinkFieldExtractor linkFieldExtractor;

	@Override
	public Set<BcField> extract(final WidgetDTO widget) {
		final Set<BcField> fields = new HashSet<>();
		if (widget.getPivotFields() != null) {
			final PivotMeta pivotMeta = JsonUtils.readValue(PivotMeta.class, widget.getPivotFields());
			pivotMeta.getRows().forEach(row -> fields.addAll(extract(widget, row)));
			pivotMeta.getCols().forEach(col -> fields.addAll(extract(widget, col)));
			pivotMeta.getValues().forEach(value -> fields.addAll(extract(widget, value)));
		}
		return fields;
	}

	private Set<BcField> extract(final WidgetDTO widget, final TableColRow tableColRow) {
		final Set<BcField> fields = linkFieldExtractor.extract(widget, tableColRow);
		if (tableColRow.getChildren() != null) {
			tableColRow.getChildren().forEach(child -> fields.addAll(extract(widget, child)));
		}
		return fields;
	}

	private Set<BcField> extract(final WidgetDTO widget, final TableValue tableValue) {
		final FieldMeta fieldMeta = tableValue.getField();
		final HashSet<BcField> fields = new HashSet<>(linkFieldExtractor.extract(widget, fieldMeta));
		fields.add(new BcField(widget.getBcName(), fieldMeta.getKey())
				.putAttribute(Attribute.WIDGET_NAME, widget.getName())
		);
		return fields;
	}

	@Override
	public List<String> getSupportedTypes() {
		List<String> result = new ArrayList<>();
		result.add("PivotFields");
		return result;
	}

}
