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

package org.cxbox.meta.ui.field;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;

/**
 * A wrapper for {@link FieldExtractor} that determines whether to automatically add the {@code id} field
 * to the API response when no business component (BC) fields are added to the widgets on the screen.
 */
@RequiredArgsConstructor
public class FieldExtractorWrapperWithId implements FieldExtractor {

	@NonNull
	private final FieldExtractor origFieldExtractor;

	@Override
	public List<String> getSupportedTypes() {
		return origFieldExtractor.getSupportedTypes();
	}

	@Override
	public int getPriority() {
		return origFieldExtractor.getPriority();
	}

	@Override
	public Set<BcField> extract(@NonNull WidgetDTO widget) {
		final Set<BcField> bcFields = origFieldExtractor.extract(widget);
		bcFields.addAll(idField(widget));
		return bcFields;
	}

	private HashSet<BcField> idField(@NonNull WidgetDTO widget) {
		final HashSet<BcField> fields = new HashSet<>();
		if (!StringUtils.isBlank(widget.getBcName())) {
			final BcField idField = new BcField(widget.getBcName(), "id")
					.putAttribute(Attribute.WIDGET_NAME, widget.getName());
			fields.add(idField);
		}
		return fields;
	}

	@Override
	public String toString() {
		return "FieldExtractorWrapperWithId(wrapping: " + origFieldExtractor.getClass().getSimpleName() + ")";
	}

}
