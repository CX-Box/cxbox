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
import java.util.Objects;
import java.util.Set;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;
import org.cxbox.meta.ui.model.json.Condition;
import org.cxbox.meta.ui.model.json.Condition.IConditionFieldEqualityParams;
import org.springframework.stereotype.Component;

@Component
public class ShowConditionFieldExtractor implements FieldExtractor {

	@Override
	public Set<BcField> extract(final WidgetDTO widget) {
		final Set<BcField> fields = new HashSet<>();
		if (!Objects.equals(widget.getShowCondition(), "[]")) {
			final Condition condition = JsonUtils.readValue(Condition.class, widget.getShowCondition());
			if (condition.getParams() != null) {
				fields.add(getField(widget, condition, condition.getParams()));
			}
			if (condition.getMultipleParams() != null) {
				for (IConditionFieldEqualityParams multipleParam : condition.getMultipleParams()) {
					fields.add(getField(widget, condition, multipleParam));
				}
			}
		}
		return fields;
	}

	private BcField getField(final WidgetDTO widget, final Condition condition,
			final IConditionFieldEqualityParams params) {
		final String bc = condition.getBcName() == null ? widget.getBcName() : condition.getBcName();
		return new BcField(bc, params.getFieldKey())
				.putAttribute(Attribute.WIDGET_NAME, widget.getName());
	}

	@Override
	public List<String> getSupportedTypes() {
		List<String> result = new ArrayList<>();
		result.add("ShowConditionFields");
		return result;
	}

}
