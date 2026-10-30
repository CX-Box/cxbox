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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.metafieldsecurity.WidgetUtils;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO.ViewWidgetSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.ui.field.FieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.validation.MetaValidator.LoadContext;
import org.cxbox.meta.validation.MetaValidator.RequestContext;
import org.junit.jupiter.api.Test;

class WidgetFieldExtractorValidatorTest {

	private final WidgetFieldExtractorValidator validator = new WidgetFieldExtractorValidator(
			new WidgetUtils(List.of(new ListExtractor()), new MetaConfigurationProperties())
	);

	@Test
	void validateOnLoadReportsWidgetWithBcAndWithoutExtractor() {
		var messages = validator.validateOnLoad(new LoadContext(List.of(), List.of(
				sourceView("first", "withExtractor", "withoutExtractor"),
				sourceView("second", "withoutExtractor"),
				sourceView("other", "withoutType")
		), widgets(
				sourceWidget("withExtractor", "List", "bc"),
				sourceWidget("withoutExtractor", "Unknown", "bc"),
				sourceWidget("withoutBc", "Unknown", null),
				sourceWidget("withoutType", null, "bc")
		)));

		assertThat(messages).hasSize(2).contains("Widget \"withoutExtractor\" (type \"Unknown\", bc \"bc\") "
				+ "has no FieldExtractor, "
				+ "so its fields will be hidden on views \"first\", \"second\". "
				+ "Please register org.cxbox.meta.ui.field.FieldExtractor bean supporting type \"Unknown\"");
		assertThat(messages).anySatisfy(message -> assertThat(message)
				.startsWith("Widget \"withoutType\" (type \"null\", bc \"bc\") ")
				.contains(" hidden on view \"other\". "));
	}

	@Test
	void validateOnLoadReportsWidgetNotPlacedOnView() {
		var messages = validator.validateOnLoad(new LoadContext(List.of(), List.of(sourceView("view")), widgets(
				sourceWidget("withoutExtractor", "Unknown", "bc")
		)));

		assertThat(messages).singleElement().asString().contains(" hidden, widget is not placed on any view. ");
	}

	@Test
	void validateOnRequestReportsOnlyWidgetsOfRequestedBc() {
		var view = new ViewDTO().setName("view").setWidgets(List.of(
				widget("withExtractor", "List", "bc"),
				widget("withoutExtractor", "Unknown", "bc"),
				widget("otherBc", "Unknown", "otherBc")
		));

		var messages = validator.validateOnRequest(new RequestContext(
				bc("bc"), "screen", new ScreenDTO(), List.of("view"), List.of(view)
		));

		assertThat(messages).singleElement().asString()
				.startsWith("Widget \"withoutExtractor\" (type \"Unknown\", bc \"bc\") ")
				.contains(" hidden on view \"view\". ");
	}

	private static ViewSourceDTO sourceView(String name, String... widgetNames) {
		var view = new ViewSourceDTO();
		view.setName(name);
		view.setWidgets(Arrays.stream(widgetNames).map(widgetName -> {
			var widget = new ViewWidgetSourceDTO();
			widget.setWidgetName(widgetName);
			return widget;
		}).toList());
		return view;
	}

	private static Map<WidgetSourceDTO, String> widgets(WidgetSourceDTO... widgets) {
		return Arrays.stream(widgets).collect(Collectors.toMap(widget -> widget, widget -> "{}"));
	}

	private static WidgetSourceDTO sourceWidget(String name, String type, String bc) {
		var widget = new WidgetSourceDTO();
		widget.setName(name);
		widget.setType(type);
		widget.setBc(bc);
		return widget;
	}

	private static WidgetDTO widget(String name, String type, String bc) {
		var widget = new WidgetDTO();
		widget.setName(name);
		widget.setType(type);
		widget.setBcName(bc);
		return widget;
	}

	private static BcIdentifier bc(String name) {
		return new BcIdentifier() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getParentName() {
				return null;
			}
		};
	}

	private static class ListExtractor implements FieldExtractor {

		@Override
		public Set<BcField> extract(WidgetDTO widget) {
			return Set.of();
		}

		@Override
		public List<String> getSupportedTypes() {
			return List.of("List");
		}

	}

}
