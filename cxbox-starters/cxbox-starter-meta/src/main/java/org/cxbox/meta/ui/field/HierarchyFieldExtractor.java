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

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.cxbox.meta.ui.field.link.LinkFieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;
import org.cxbox.meta.ui.model.json.WidgetOptions;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.meta.entity.Widget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HierarchyFieldExtractor implements FieldExtractor {

	private final LinkFieldExtractor linkFieldExtractor;

	@Override
	public Set<BcField> extract(Widget widget) {
		final HashSet<BcField> fields = new HashSet<>();
		Optional.ofNullable(widget.getOptions())
				.map(JsonUtils::readTree)
				.filter(JsonNode::isObject)
				.map(options -> JsonUtils.readValue(WidgetOptions.class, options))
				.map(WidgetOptions::getHierarchy)
				.ifPresent(list -> list.forEach(item -> {
					if (item.getAssocValueKey() != null) {
						fields.add(new BcField(item.getBcName(), item.getAssocValueKey())
								.putAttribute(Attribute.WIDGET_ID, widget.getId()));
					}
					item.getFields().forEach(field -> {
								fields.add(new BcField(item.getBcName(), field.getKey())
										.putAttribute(Attribute.WIDGET_ID, widget.getId()));
								fields.addAll(linkFieldExtractor.extract(widget.getId(), item.getBcName(), field));
							});
						}
				));
		return fields;
	}

	@Override
	public List<String> getSupportedTypes() {
		List<String> result = new ArrayList<>();
		result.add("HierarchyFields");
		return result;
	}

}
