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

package org.cxbox.meta.validation;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.cxbox.meta.metafieldsecurity.WidgetUtils;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.ui.field.FieldExtractor;
import org.springframework.stereotype.Component;

/**
 * Warns about widgets with bc whose type has no {@link FieldExtractor}: fields of such widgets are hidden
 * by field level security, so widget shows no data.
 * <p>
 * Example:
 * <pre>
 * Widget "clientInfo" (type "CustomInfo", bc "client") has no FieldExtractor, so its fields will be hidden on view
 * "clientview". Please register org.cxbox.meta.ui.field.FieldExtractor bean supporting type "CustomInfo"
 * </pre>
 * To fix:
 * <ul>
 *   <li>typo in type - fix {@code type} in {@code *.widget.json}</li>
 *   <li>custom widget type - register Spring bean implementing {@link FieldExtractor} (in any package of project)
 *   with this type in {@link FieldExtractor#getSupportedTypes()}. Usually it is enough to extend existing extractor
 *   of similar widget, e.g. {@link org.cxbox.meta.ui.field.SimpleFieldExtractor} for form-like widgets or
 *   {@link org.cxbox.meta.ui.field.ListFieldExtractor} for list-like widgets:
 * <pre>{@code
 * @Component
 * public class CustomInfoFieldExtractor extends SimpleFieldExtractor {
 *
 *   public CustomInfoFieldExtractor(LinkFieldExtractor linkFieldExtractor) {
 *     super(linkFieldExtractor);
 *   }
 *
 *   @Override
 *   public List<String> getSupportedTypes() {
 *     return List.of("CustomInfo");
 *   }
 *
 * }
 * }</pre>
 *   </li>
 * </ul>
 * Checked on load (all widgets, with their views) and on row-meta request (widgets of requested bc on opened screen).
 * Bean name for {@link org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties.Validation#getInclude()
 * cxbox.meta.validation.include}: {@code widgetFieldExtractorValidator}.
 */
@Component
@RequiredArgsConstructor
public class WidgetFieldExtractorValidator implements MetaValidator {

	private final WidgetUtils widgetUtils;

	@Override
	public List<String> validateOnLoad(LoadContext context) {
		return context.widgets().keySet().stream()
				.filter(widget -> widget.getBc() != null && !widgetUtils.hasFieldExtractor(widget.getType()))
				.map(widget -> message(
						widget.getName(), widget.getType(), widget.getBc(), views(context.views(), widget.getName())
				))
				.toList();
	}

	@Override
	public List<String> validateOnRequest(RequestContext context) {
		return context.views().stream()
				.flatMap(view -> view.getWidgets().stream()
						.filter(widget -> Objects.equals(context.bc().getName(), widget.getBcName()))
						.filter(widget -> !widgetUtils.hasFieldExtractor(widget.getType()))
						.map(widget -> message(
								widget.getName(), widget.getType(), widget.getBcName(),
								" on view \"" + view.getName() + "\""
						)))
				.toList();
	}

	private String views(List<ViewSourceDTO> views, String widgetName) {
		var viewNames = views.stream()
				.filter(view -> view.getWidgets().stream()
						.anyMatch(widget -> Objects.equals(widgetName, widget.getWidgetName())))
				.map(view -> "\"" + view.getName() + "\"")
				.toList();
		if (viewNames.isEmpty()) {
			return ", widget is not placed on any view";
		}
		return (viewNames.size() == 1 ? " on view " : " on views ") + String.join(", ", viewNames);
	}

	private String message(String widgetName, String widgetType, String bcName, String views) {
		return "Widget \"" + widgetName + "\" (type \"" + widgetType + "\", bc \"" + bcName + "\") "
				+ "has no FieldExtractor, "
				+ "so its fields will be hidden" + views + ". "
				+ "Please register " + FieldExtractor.class.getName() + " bean supporting type \"" + widgetType + "\"";
	}

}
