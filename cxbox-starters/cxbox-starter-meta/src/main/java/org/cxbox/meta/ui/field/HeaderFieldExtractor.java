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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.util.i18n.LocalizationFormatter;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.field.link.LinkFieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.json.field.FieldMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HeaderFieldExtractor extends BaseFieldExtractor {

	public HeaderFieldExtractor(@Autowired LinkFieldExtractor linkFieldExtractor) {
		super(linkFieldExtractor);
	}

	@Override
	public Set<BcField> extract(WidgetDTO widget) {
		final Set<BcField> titleFields = extractFieldsFromTitle(widget, LocalizationFormatter.i18n(widget.getTitle()));
		final Set<BcField> widgetFields = new HashSet<>(titleFields);
		if (widget.getFields() == null) {
			return widgetFields;
		}
		// only fields shown in the title: their bgColorKey, drillDownKey and other links
		for (final JsonNode field : JsonUtils.readTree(widget.getFields())) {
			final JsonNode key = field.path("key");
			if (key.isTextual() && titleFields.contains(new BcField(widget.getBcName(), key.textValue()))) {
				widgetFields.addAll(extractTitleField(widget, key.textValue(), field));
			}
		}
		return widgetFields;
	}

	/**
	 * Earlier "fields" of a header were not read at all.
	 * So a field with broken meta is skipped: the title works as before, only without bgColorKey and other links.
	 */
	private Set<BcField> extractTitleField(WidgetDTO widget, String key, JsonNode field) {
		try {
			return extract(widget, JsonUtils.readValue(FieldMeta.class, field));
		} catch (Exception e) {
			log.warn("HeaderWidget \"{}\": field \"{}\" is skipped, its meta can not be read: {}", widget.getName(), key, e.getMessage());
			return Collections.emptySet();
		}
	}

	@Override
	public List<String> getSupportedTypes() {
		List<String> result = new ArrayList<>();
		result.add("HeaderWidget");
		return result;
	}

}
