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
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssocListFieldExtractor implements FieldExtractor {

	private final ListFieldExtractor linkFieldExtractor;

	@Override
	public Set<BcField> extract(final WidgetDTO widget) {
		final Set<BcField> widgetFields = new HashSet<>(linkFieldExtractor.extract(widget));
		widgetFields.add(new BcField(widget.getBcName(), BcField.FIELD_ASSOCIATE)
				.putAttribute(Attribute.WIDGET_NAME, widget.getName())
		);
		return widgetFields;
	}

	@Override
	public List<String> getSupportedTypes() {
		List<String> result = new ArrayList<>();
		result.add("AssocListPopup");
		return result;
	}

}
