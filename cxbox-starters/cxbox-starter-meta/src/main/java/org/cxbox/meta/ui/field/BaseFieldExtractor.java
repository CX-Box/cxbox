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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.cxbox.api.util.i18n.LocalizationFormatter;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.ui.field.link.LinkFieldExtractor;
import org.cxbox.meta.ui.model.BcField;
import org.cxbox.meta.ui.model.BcField.Attribute;
import org.cxbox.meta.ui.model.MultivalueField;
import org.cxbox.meta.ui.model.PickListField;
import org.cxbox.meta.ui.model.json.field.FieldMeta;
import org.cxbox.meta.ui.model.json.field.FieldMeta.FieldMetaBase.MultiSourceInfo;
import org.cxbox.meta.ui.model.json.field.subtypes.MultivalueFieldMeta;
import org.cxbox.meta.ui.model.json.field.subtypes.PickListFieldMeta;


public abstract class BaseFieldExtractor implements FieldExtractor {

	private final LinkFieldExtractor linkFieldExtractor;

	protected BaseFieldExtractor(LinkFieldExtractor linkFieldExtractor) {
		this.linkFieldExtractor = linkFieldExtractor;
	}

	protected Set<BcField> extract(final WidgetDTO widget, final FieldMeta fieldMeta) {
		final Set<BcField> widgetFields = new HashSet<>();
		final Set<BcField> pickListFields = new HashSet<>();
		if (fieldMeta instanceof FieldMeta.FieldContainer fieldContainer && fieldContainer.getChildren() != null) {
			for (final FieldMeta child : fieldContainer.getChildren()) {
				widgetFields.addAll(extract(widget, child));
			}
		}
		if (fieldMeta instanceof FieldMeta.FieldMetaBase fieldMetaBase) {
			for (final PickListField pickList : getPickLists(fieldMetaBase)) {
				if (pickList.getPickMap() != null) {
					for (final Entry<String, String> entry : pickList.getPickMap().entrySet()) {
						widgetFields.add(new BcField(widget.getBcName(), entry.getKey())
								.putAttribute(Attribute.WIDGET_NAME, widget.getName())
						);
						pickListFields.add(new BcField(pickList.getPickListBc(), entry.getValue())
								.putAttribute(Attribute.WIDGET_NAME, widget.getName())
								.putAttribute(Attribute.PARENT_BC, widget.getBcName())
								.putAttribute(Attribute.PARENT_FIELD, entry.getKey())
						);
					}
				}
			}
			widgetFields.addAll(extractFieldsFromMultiValue(widget, getMultivalueField(fieldMetaBase)));
			widgetFields.addAll(extractFieldsFromTitle(widget, LocalizationFormatter.i18n(fieldMetaBase.getTitle())));
			widgetFields.addAll(linkFieldExtractor.extract(widget, fieldMetaBase));
			if (fieldMetaBase.getMultisource() != null) {
				for (final MultiSourceInfo multiSourceInfo : fieldMetaBase.getMultisource()) {
					widgetFields.add(new BcField(widget.getBcName(), multiSourceInfo.getKey())
							.putAttribute(Attribute.WIDGET_NAME, widget.getName())
					);
				}
			}
			final BcField widgetField = new BcField(widget.getBcName(), fieldMetaBase.getKey())
					.putAttribute(Attribute.WIDGET_NAME, widget.getName())
					.putAttribute(Attribute.TYPE, fieldMetaBase.getType())
					.putAttribute(Attribute.ICON_TYPE_KEY, fieldMetaBase.getIconTypeKey())
					.putAttribute(Attribute.HINT_KEY, fieldMetaBase.getHintKey())
					.putAttribute(Attribute.PICK_LIST_FIELDS, pickListFields);
			widgetFields.remove(widgetField);
			widgetFields.add(widgetField);
		}
		return widgetFields;
	}

	private List<BcField> extractFieldsFromMultiValue(WidgetDTO widget, MultivalueField multivalueField) {
		List<BcField> result = new ArrayList<>();
		if (multivalueField == null) {
			return result;
		}
		if (multivalueField.getAssocValueKey() != null) {
			result.add(new BcField(multivalueField.getPopupBcName(), multivalueField.getAssocValueKey())
					.putAttribute(Attribute.WIDGET_NAME, widget.getName())
			);
		}
		if (multivalueField.getDisplayedKey() != null) {
			result.add(new BcField(widget.getBcName(), multivalueField.getDisplayedKey())
					.putAttribute(Attribute.WIDGET_NAME, widget.getName())
			);
		}
		return result;
	}

	private MultivalueField getMultivalueField(final FieldMeta.FieldMetaBase field) {
		if (field.getType().equals("multivalue") || field.getType().equals("multivalueHover")) {
			final MultivalueFieldMeta multivalueField = (MultivalueFieldMeta) field;
			return new MultivalueField(
					multivalueField.getPopupBcName(),
					multivalueField.getAssocValueKey(),
					multivalueField.getDisplayedKey(),
					multivalueField.getAssociateFieldKey()
			);
		}
		return null;
	}

	@NonNull
	private List<PickListField> getPickLists(@NonNull final FieldMeta.FieldMetaBase field) {
		final List<PickListField> pickLists = new ArrayList<>();
		if (field instanceof PickListFieldMeta pickListField) {
			pickLists.add(new PickListField(pickListField.getPopupBcName(), pickListField.getPickMap()));
		}
		return pickLists;
	}

	protected Set<BcField> extractFieldsFromTitle(final WidgetDTO widget, final String title) {
		final HashSet<BcField> fields = new HashSet<>();
		if (title == null) {
			return fields;
		}
		for (var fieldKey : fieldKeys(title)) {
			fields.add(new BcField(widget.getBcName(), fieldKey)
					.putAttribute(Attribute.WIDGET_NAME, widget.getName())
			);
		}
		return fields;
	}

	@NonNull
	public List<String> fieldKeys(String template) {
		List<String> valueList = new ArrayList<>();
		Matcher matcher = Pattern.compile("[$][{](\\w+)}").matcher(template);
		while (matcher.find()) {
			String key = matcher.group(1);
			valueList.add(key);
		}
		return valueList;
	}

}
