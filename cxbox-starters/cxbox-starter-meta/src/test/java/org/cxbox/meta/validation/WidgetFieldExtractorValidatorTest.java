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
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.dto.MetaValidationResult;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.metafieldsecurity.WidgetUtils;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO.ViewWidgetSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.ui.field.FieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.LogLevel;

class WidgetFieldExtractorValidatorTest {

	private final WidgetFieldExtractorValidator validator = new WidgetFieldExtractorValidator(
			new WidgetUtils(List.of(new ListExtractor()), new MetaConfigurationProperties())
	);

	@Test
	void reportsWidgetWithBcAndWithoutExtractor() {
		var problems = validate(
				List.of(
						sourceView("first", "withExtractor", "withoutExtractor"),
						sourceView("second", "withoutExtractor"),
						sourceView("other", "withoutType")
				), widgets(
						sourceWidget("withExtractor", "List", "bc"),
						sourceWidget("withoutExtractor", "Unknown", "bc"),
						sourceWidget("withoutBc", "Unknown", null),
						sourceWidget("withoutType", null, "bc")
				)
		);

		assertThat(problems).extracting(MetaValidationResult::getWidgets, MetaValidationResult::getViews).containsExactly(
				tuple(Set.of("withoutExtractor"), Set.of("first", "second")),
				tuple(Set.of("withoutType"), Set.of("other"))
		);
		assertThat(problems.get(0).getMessage())
				.isEqualTo("Type \"Unknown\" (bc \"bc\") is unknown to FieldExtractor beans, fields are hidden");
		assertThat(problems.get(0).getServerLogDetails()).isEqualTo("Fix type of widget to one of known types or, "
				+ "for custom widget type, register org.cxbox.meta.ui.field.FieldExtractor bean supporting it.");
		assertThat(problems.get(1).getMessage()).startsWith("Type \"null\" (bc \"bc\") ");
	}

	@Test
	void reportsWidgetNotPlacedOnView() {
		var problems = validate(
				List.of(sourceView("view")), widgets(
						sourceWidget("withoutExtractor", "Unknown", "bc")
				)
		);

		assertThat(problems).singleElement().satisfies(problem -> {
			assertThat(problem.getWidgets()).containsExactly("withoutExtractor");
			assertThat(problem.getViews()).isEmpty();
		});
	}

	@Test
	void viewWithoutWidgetsIsSkipped() {
		var empty = new ViewSourceDTO();
		empty.setName("empty");
		var problems = validate(
				List.of(empty, sourceView("view", "withoutExtractor")),
				widgets(sourceWidget("withoutExtractor", "Unknown", "bc"))
		);

		assertThat(problems).singleElement().extracting(MetaValidationResult::getViews).isEqualTo(Set.of("view"));
	}

	private List<MetaValidationResult> validate(List<ViewSourceDTO> views, Map<WidgetSourceDTO, String> widgets) {
		return new MetaValidationService(Map.of("validator", validator), mock(TransactionService.class))
				.runValidators(List.of(), views, widgets, LogLevel.OFF);
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
