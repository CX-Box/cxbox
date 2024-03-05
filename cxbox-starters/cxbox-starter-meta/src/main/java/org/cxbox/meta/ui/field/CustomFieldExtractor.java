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

import java.util.HashMap;
import java.util.Optional;
import javax.naming.OperationNotSupportedException;
import lombok.RequiredArgsConstructor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;
import org.cxbox.meta.ui.model.json.PivotMeta.TableColRow;
import org.cxbox.meta.ui.model.json.field.FieldMeta.FieldMetaBase;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomFieldExtractor {

	private final Optional<CustomFields> customFields;

	@SneakyThrows
	public Set<BcField> extract(final String widgetName, final String bc, final Object meta) {
		final Set<BcField> fields = new HashSet<>();
		if (!customFields.isPresent()) {
			return fields;
		}
		CustomFields service = customFields.get();
		Map<String, String> custom = new HashMap<>();
		if (meta instanceof FieldMetaBase) {
			custom = ((FieldMetaBase) meta).getCustomFields();
		} else if (meta instanceof TableColRow) {
			//TODO>>remove exception and write mapper if PivotMeta support needed
			throw new OperationNotSupportedException("meta type PivotMeta is not supported");
		}
		List<String> fieldNames = service.getFieldNames();

		for (Entry<String, String> entry : custom.entrySet()) {
			if (!fieldNames.contains(entry.getKey())) {
				throw new IllegalArgumentException("Not found field with key:" + entry.getKey());
			}

			fields.add(new BcField(bc, entry.getValue())
					.putAttribute(Attribute.WIDGET_NAME, widgetName)
			);
		}

		return fields;
	}

}
