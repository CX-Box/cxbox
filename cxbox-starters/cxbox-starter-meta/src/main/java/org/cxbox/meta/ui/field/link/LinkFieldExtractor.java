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

package org.cxbox.meta.ui.field.link;

import jakarta.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cxbox.core.util.InstrumentationAwareReflectionUtils;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.field.CustomFieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class LinkFieldExtractor {

	private final CustomFieldExtractor customFieldExtractor;

	@SneakyThrows
	public Set<BcField> extract(final WidgetDTO widget, final Object object) {
		return extract(widget.getName(), widget.getBcName(), object);
	}

	@SneakyThrows
	public Set<BcField> extract(final String widgetName, final String bc, final Object object) {
		final Set<BcField> fields = new HashSet<>();
		AtomicInteger maxDepth = new AtomicInteger(5);
		recursiveExtractLinkToFields(widgetName, bc, object, fields, maxDepth);
		Set<BcField> customFields = customFieldExtractor.extract(widgetName, bc, object);
		fields.addAll(customFields);
		return fields;
	}

	private static void recursiveExtractLinkToFields(String widgetName, String bc, @Nullable Object object, Set<BcField> fields,
			AtomicInteger depth)
			throws IllegalAccessException {
		if (object  ==  null) {
			return;
		}
		if (depth.decrementAndGet() <= 0) {
			return;
		}
		for (final Field field : InstrumentationAwareReflectionUtils.getAllNonSyntheticFieldsList(object.getClass())) {
			field.setAccessible(true);
			if (field.isAnnotationPresent(LinkToField.class) && field.get(object) != null) {
				fields.add(new BcField(bc, (String) field.get(object))
						.putAttribute(Attribute.WIDGET_NAME, widgetName)
				);
			} else {
				recursiveExtractLinkToFields(widgetName, bc, field.get(object), fields, depth);
			}
		}
	}

}
