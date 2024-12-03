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

import static org.cxbox.api.data.dictionary.DictionaryCache.dictionary;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.dictionary.IDictionaryType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.Icon;
import org.cxbox.constgen.DtoField;
import org.cxbox.dictionary.DictionaryProvider;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class FieldsDictionaryDeprecatedMeta<T extends DataResponseDTO> extends FieldsCommonMeta<T> {

	public FieldsDictionaryDeprecatedMeta(@Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objectMapper, Optional<DictionaryProvider> dictionaryProvider) {
		super(objectMapper, dictionaryProvider);
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link FieldsMeta#setDictionaryFilterValues(DtoField)} instead
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void setAllFilterValuesByLovType(DtoField<? super T, ?> field, IDictionaryType type) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearFilterValues();
					fieldDTO.setFilterValues(dictionary().getAll(type));
				});
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link FieldsMeta#setDictionaryFilterValues(DtoField)} instead
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void setAllFilterValuesByLovType(
			final DtoField<?, ?> field,
			@NonNull final IDictionaryType type,
			@NonNull final Comparator<SimpleDictionary> comparator) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearFilterValues();
					fieldDTO.setFilterValues(dictionary().getAll(type)
							.stream()
							.filter(Objects::nonNull)
							.sorted(comparator)
							.toList()
					);
				});
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link FieldsMeta#setDictionaryIcons(DtoField, Map)} instead
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void setAllValuesWithIcons(
			DtoField<? super T, ?> field,
			IDictionaryType type,
			Map<LOV, Icon> valueIconMap) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearAllValues();
					valueIconMap
							.forEach((key, value) ->
									fieldDTO.setIconWithValue(type.lookupValue(key), value)
							);
				});
	}


	/**
	 * @deprecated Since 4.0.0-M12 use {@link FieldsMeta#setEnumIcons(DtoField, Map)} instead
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final <E extends Enum<?>> void setAllValuesWithIcons(@Nullable DtoField<? super T, E> field,
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
