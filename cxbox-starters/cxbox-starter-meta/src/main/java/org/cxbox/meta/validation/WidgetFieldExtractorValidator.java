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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.cxbox.dto.MetaValidationResult;
import org.cxbox.meta.metafieldsecurity.WidgetUtils;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.ui.field.FieldExtractor;
import org.springframework.stereotype.Component;

/**
 * Reports BC widgets whose type has no {@link FieldExtractor}.
 * Field security hides fields of these widgets. Register an extractor for custom widget types.
 */
@Component
@RequiredArgsConstructor
public class WidgetFieldExtractorValidator implements MetaValidator {

	private static final String MISSING_EXTRACTOR_HINT =
			"Fix type of widget to one of known types or, for custom widget type, register "
					+ FieldExtractor.class.getName() + " bean supporting it.";

	private final WidgetUtils widgetUtils;

	@Override
	public List<MetaValidationResult> validate(List<ScreenSourceDto> screens, List<ViewSourceDTO> viewDtos,
			Map<WidgetSourceDTO, String> widgets) {
		return widgets.keySet().stream()
				.filter(widget -> widget.getBc() != null && !widgetUtils.hasFieldExtractor(widget.getType()))
				.sorted(Comparator.comparing(WidgetSourceDTO::getName, Comparator.nullsLast(Comparator.naturalOrder())))
				.map(widget -> MetaValidationResult.forWidget(widget.getName(), missingExtractorMessage(widget))
						.setServerLogDetails(MISSING_EXTRACTOR_HINT))
				.toList();
	}

	@Override
	public String title() {
		return "Widget type without FieldExtractor";
	}

	private String missingExtractorMessage(WidgetSourceDTO widget) {
		return "Type \"" + widget.getType() + "\" (bc \"" + widget.getBc() + "\") is unknown to FieldExtractor beans, "
				+ "fields are hidden";
	}

}
