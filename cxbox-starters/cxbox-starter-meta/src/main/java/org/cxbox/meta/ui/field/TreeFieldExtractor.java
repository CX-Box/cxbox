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
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.field.link.LinkFieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;
import org.springframework.stereotype.Component;


@Component
public class TreeFieldExtractor extends ListFieldExtractor {

	public TreeFieldExtractor(LinkFieldExtractor linkFieldExtractor) {
		super(linkFieldExtractor);
	}

	@Override
	public void customizeFields(WidgetDTO widget, Set<BcField> fields) {
		fields.addAll(parentIdField(widget));
	}

	private HashSet<BcField> parentIdField(@NonNull WidgetDTO widget) {
		final HashSet<BcField> fields = new HashSet<>();
		if (!StringUtils.isBlank(widget.getBcName())) {
			final BcField parentIdField = new BcField(widget.getBcName(), "parentId")
					.putAttribute(Attribute.WIDGET_NAME, widget.getName());
			fields.add(parentIdField);
		}
		return fields;
	}

	@Override
	public List<String> getSupportedTypes() {
		List<String> result = new ArrayList<>();
		result.add("AssocTreePopup");
		result.add("PickTreePopup");
		result.add("Tree");
		return result;
	}

}
