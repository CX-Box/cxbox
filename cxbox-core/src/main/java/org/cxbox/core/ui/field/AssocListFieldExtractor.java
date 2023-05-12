
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

package org.cxbox.core.ui.field;

import org.cxbox.core.ui.model.BcField;
import org.cxbox.core.ui.model.BcField.Attribute;
import org.cxbox.model.ui.entity.Widget;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class AssocListFieldExtractor implements FieldExtractor {

	@Override
	public Set<BcField> extract(final Widget widget) {
		final Set<BcField> widgetFields = new HashSet<>(new ListFieldExtractor().extract(widget));
		widgetFields.add(new BcField(widget.getBc(), BcField.FIELD_ASSOCIATE)
				.putAttribute(Attribute.WIDGET_ID, widget.getId())
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
