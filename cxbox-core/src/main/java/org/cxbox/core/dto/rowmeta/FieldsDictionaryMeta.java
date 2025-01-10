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

package org.cxbox.core.dto.rowmeta;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.Icon;
import org.cxbox.constgen.DtoField;
import org.cxbox.dictionary.Dictionary;
import org.cxbox.dictionary.DictionaryProvider;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class FieldsDictionaryMeta<T extends DataResponseDTO> extends FieldsDictionaryDeprecatedMeta<T> {

	private final Optional<DictionaryProvider> dictionaryProvider;

	public FieldsDictionaryMeta(@Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objectMapper,
			Optional<DictionaryProvider> dictionaryProvider) {
		super(objectMapper, dictionaryProvider);
		this.dictionaryProvider = dictionaryProvider;
	}

	/**
	 * <br>
	 * <br>
	 * Fills dictionary field {@code filer} drop-down (List widget field during {@code filer} editing) with {@code all} values from {@link org.cxbox.dictionary.Dictionary#getDictionaryType() dictionary type}
	 *
	 * @param field - widget dictionary field
	 * @param <V> - dictionary class. Usually class name determines dictionary type, that is used to convert keys to values for UI (see {@link Dictionary#getDictionaryType()})
	 * @implNote If you use {@link DictionaryProvider#getAll(Class)}, that delegates to {@link org.cxbox.api.data.dictionary.DictionaryCache}, then drop-down values are sorted by display_order, then by key (display_order can be null)
	 * <p>
	 * See {@code dicts.sort} line in {@link org.cxbox.model.core.service.DictionaryCacheImpl.Cache#load()}
	 * <p>
	 * <br>
	 * Attention - sorting rows in List widgets always ignores display_order and is done by key lexicographically!
	 */
	public final <V extends Dictionary> void setDictionaryFilterValues(DtoField<? super T, V> field) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					Collection<V> variants = dictionaryProvider.map(e -> e.getAll(field.getValueClazz()))
							.orElse(new ArrayList<>());
					this.setDictionaryFilterValues(field, variants);
				});
	}

	/**
	 * <br>
	 * <br>
	 *
	 * @param field - widget dictionary field
	 * @param variants - variants to be shown in field {@code filer} drop-down (List widget field during {@code filer} editing)
	 * @param <V> - dictionary class. Usually class name determines dictionary type, that is used to convert keys to values for UI (see {@link Dictionary#getDictionaryType()})
	 */
	public final <V extends Dictionary> void setDictionaryFilterValues(@Nullable DtoField<? super T, V> field,
			@NonNull Collection<V> variants) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setDictionaryName(Dictionary.of(field.getValueClazz(), "").getDictionaryType());
					List<SimpleDictionary> dictDTOList = mapDictionary(variants);
					fieldDTO.clearValues();
					this.setConcreteFilterValues(field, dictDTOList);
				});
	}

	/**
	 * <br>
	 * <br>
	 * Adds icons for fields having "type":"dictionary" in widget.json
	 * <br>
	 * Usually one will provide mapping of ALL dictionary values to their icons here.
	 *
	 * @param field - widget dictionary field
	 * @param <V> - dictionary class. Usually class name determines dictionary type, that is used to convert keys to values for UI (see {@link Dictionary#getDictionaryType()})
	 * @param valueIconMap <V, Icon> values, that one want to have icons in UI.
	 * @implNote Filer drop-down values (if set with {@link FieldsMeta#setDictionaryFilterValues(DtoField, Collection)} or alternative) and edit drop-down values (if set with {@link RowDependentFieldsMeta#setDictionaryValues(DtoField, Collection)} or alternative) can contain {@code only subset} of all dictionary values. Current method does not fill or override these filter/edit drop-downs - it just adds icons for values that will be found in {@code valueIconMap}
	 */
	public final <V extends Dictionary> void setDictionaryIcons(
			@Nullable DtoField<? super T, V> field,
			@NonNull Map<V, Icon> valueIconMap) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearValues();
					dictionaryProvider.ifPresent(p -> {
						valueIconMap.forEach((variant, icon) -> {
							var value = p.lookupValue(variant);
							fieldDTO.setIconWithValue(value != null ? value.getValue() : null, icon);
						});
					});
				});
	}

	public final <T extends DataResponseDTO, E extends Enum<?>> void setEnumFilterValues(
			@NonNull FieldsMeta<T> fieldsMeta,
			@Nullable DtoField<? super T, E> field,
			@NonNull E... values
	) {
		if (field != null) {
			fieldsMeta.setConcreteFilterValues(field, Arrays
					.stream(values)
					.map(en -> new SimpleDictionary(en.name(), serialize(en)))
					.collect(Collectors.toList())
			);
		}
	}

	/**
	 * <br>
	 * <br>
	 * Same as {@link FieldsMeta#setDictionaryIcons} but for Enum based dictionary fields
	 *
	 * @param field dto field
	 * @param valueIconMap <extends Enum, Icon> Enum to icon mapping
	 *
	 * <br>
	 * <br>
	 * Example 1:
	 *
	 * <pre>{@code
	 * @RequiredArgsConstructor
	 * @Getter
	 * public enum IconsEnum implements Icon {
	 *  ARROW_UP("arrow-up #0cbfe9"),
	 *  WATERMELON("watermelon"),
	 *  DOWN("down");
	 *
	 * private final String icon;
	 * }}</pre>
	 * <pre>{@code
	 * @Getter
	 * @AllArgsConstructor
	 * public enum CustomFieldDictionaryEnum {
	 *
	 *  HIGH("High", IconsEnum.ARROW_UP),
	 *  MIDDLE("Middle", IconsEnum.DOWN),
	 *  LOW("Low", IconsEnum.WATERMELON);
	 *
	 *  @JsonValue
	 *  private final String value;
	 *
	 *  private final Icon icon;
	 *
	 *  public static Map<CustomFieldDictionaryEnum, Icon> iconMap() {
	 *      return Arrays.stream(CustomFieldDictionaryEnum.values())
	 *      .filter(e -> e.icon != null)
	 *      .collect(Collectors.toMap(e -> e, e -> e.icon));
	 *  }
	 *
	 * }}</pre>
	 * Add to buildIndependentMeta
	 * <pre>{@code
	 *  fields.setEnumIcons(MyExampleDTO_.customFieldDictionary, CustomFieldDictionaryEnum.iconMap());
	 * }</pre>
	 */
	public final <E extends Enum<?>> void setEnumIcons(@Nullable DtoField<? super T, E> field,
			@NonNull Map<E, Icon> valueIconMap) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
							fieldDTO.clearAllValues();
							valueIconMap
									.forEach((key, value) ->
											fieldDTO.setIconWithValue(serialize(key), value)
									);
						}
				);
	}

}
