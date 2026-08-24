/*
 * © OOO "SI IKS LAB", 2022-2026
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

package org.cxbox.meta.ui.field.tree;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.field.link.LinkFieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.json.WidgetOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TreeDefaultFieldExtractor {

	public static final Map<String, String> DEFAULT_FIELDS = Map.of(
			"parentFieldKey", "parentId",
			"isLeafFieldKey", "isLeaf"
	);

	private final LinkFieldExtractor linkFieldExtractor;

	public TreeDefaultFieldExtractor(@Autowired LinkFieldExtractor linkFieldExtractor) {
		this.linkFieldExtractor = linkFieldExtractor;
	}

	public Set<BcField> extractFieldsFromOptions(final WidgetDTO widget) {
		WidgetOptions options = linkFieldExtractor.extractWidgetOptions(widget);
		if (options == null) {
			return Collections.emptySet();
		}
		Set<BcField> result = new HashSet<>();
		if (options.getTree() == null) {
			// add default tree fields if they were not overridden in the options
			DEFAULT_FIELDS.forEach((fieldKey, field) ->
					result.add(linkFieldExtractor.createField(widget, field)));
		} else {
			// add fields overridden in the options
			Set<BcField> treeFields = linkFieldExtractor.extract(
					widget.getName(), widget.getBcName(), options.getTree()
			);
			result.addAll(treeFields);
			// add default tree fields if they were not overridden in the options
			DEFAULT_FIELDS.forEach((fieldKey, field) -> {
				if (treeFields.stream().noneMatch(treeField -> fieldKey.equals(treeField.getName()))) {
					result.add(linkFieldExtractor.createField(widget, field));
				}
			});
		}
		return result;
	}

}
