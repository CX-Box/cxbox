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

package org.cxbox.meta.metafieldsecurity;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.field.FieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;
import org.springframework.stereotype.Component;

@Component
public final class WidgetUtils {

	private final Map<String, FieldExtractor> fieldExtractorMap;

	public WidgetUtils(List<FieldExtractor> fieldExtractors) {
		HashMap<String, FieldExtractor> extractors = new HashMap();
		fieldExtractors
				.forEach(extractor -> extractor.getSupportedTypes().forEach(type ->
						extractors.compute(type, (key, current) -> Stream.of(current, extractor)
								.filter(Objects::nonNull)
								.min(Comparator.comparing(
										FieldExtractor::getPriority
								)).orElse(extractor))
				));
		fieldExtractorMap = Collections.unmodifiableMap(extractors);
	}

	public Set<BcField> extractFields(final WidgetDTO widget) {
		final FieldExtractor fieldExtractor = fieldExtractorMap.get(widget.getType());
		if (fieldExtractor == null) {
			return Collections.emptySet();
		}
		return fieldExtractor.extract(widget);
	}

	public Set<BcField> extractPickListFields(final WidgetDTO widget) {
		return extractFields(widget).stream()
				.map(field -> field.<Set<BcField>>getAttribute(Attribute.PICK_LIST_FIELDS))
				.filter(e -> e != null && !e.isEmpty())
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	public Set<BcField> extractShowConditionFields(final WidgetDTO widget) {
		return fieldExtractorMap.get("ShowConditionFields").extract(widget);
	}

	public Set<BcField> extractPivotFields(final WidgetDTO widget) {
		return fieldExtractorMap.get("PivotFields").extract(widget);
	}

	public Set<BcField> extractChartFields(final WidgetDTO widget) {
		return fieldExtractorMap.get("ChartFields").extract(widget);
	}

	public Set<BcField> extractHierarchyFields(final WidgetDTO widget) {
		return fieldExtractorMap.get("HierarchyFields").extract(widget);
	}

	public Set<BcField> extractAllFields(final WidgetDTO widget) {
		final Set<BcField> fields = new HashSet<>(extractFields(widget));
		fields.addAll(extractShowConditionFields(widget));
		fields.addAll(extractPivotFields(widget));
		fields.addAll(extractChartFields(widget));
		fields.addAll(extractHierarchyFields(widget));
		fields.addAll(
				fields.stream()
						.map(field -> field.<Set<BcField>>getAttribute(Attribute.PICK_LIST_FIELDS))
						.filter(e -> e != null && !e.isEmpty())
						.flatMap(Collection::stream)
						.collect(Collectors.toSet())
		);
		return fields;
	}

}
