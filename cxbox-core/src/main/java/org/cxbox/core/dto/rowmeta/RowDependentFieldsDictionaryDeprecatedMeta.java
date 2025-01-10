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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.api.data.dictionary.IDictionaryType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class RowDependentFieldsDictionaryDeprecatedMeta<T extends DataResponseDTO> extends
		RowDependentFieldsCommonMeta<T> {

	public RowDependentFieldsDictionaryDeprecatedMeta(@Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objectMapper) {
		super(objectMapper);
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link RowDependentFieldsDictionaryMeta#setDictionaryValues(DtoField, Collection)} instead
	 */
	@Deprecated(since = "4.0.0-M12")
	public final void setDictionaryTypeWithConcreteValuesFromList(DtoField<? super T, ?> field, IDictionaryType type,
			List<LOV> lovs) {
		String[] keys = lovs.stream()
				.filter(Objects::nonNull)
				.map(LOV::getKey)
				.toArray(String[]::new);
		setDictionaryTypeWithConcreteValues(field, type, keys);
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link RowDependentFieldsDictionaryMeta#setDictionaryValues(DtoField)} instead}
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void setDictionaryTypeWithAllValues(DtoField<? super T, ?> field, IDictionaryType type) {
		setDictionaryTypeWithAllValues(field, type.getName());
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link RowDependentFieldsDictionaryMeta#setDictionaryValues(DtoField)} instead}
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void setDictionaryTypeWithAllValues(DtoField<? super T, ?> field, String type) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setDictionaryName(type);
					fieldDTO.clearValues();
					fieldDTO.setValues(dictionary().getAll(type));
				});
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link RowDependentFieldsDictionaryMeta#setDictionaryValues(DtoField)} instead}
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	private void setDictionaryTypeWithAllValues(
			final DtoField<?, ?> field,
			@NonNull final String type,
			@NonNull final Comparator<SimpleDictionary> comparator) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName())).ifPresent(fieldDTO -> {
			fieldDTO.setDictionaryName(type);
			fieldDTO.clearValues();
			fieldDTO.setValues(DictionaryCache.dictionary().getAll(type).stream().sorted(comparator).toList());
		});
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link RowDependentFieldsDictionaryMeta#setDictionaryValues(DtoField, Collection))))} instead
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void setDictionaryTypeWithConcreteValues(DtoField<? super T, ?> field, IDictionaryType type,
			String... keys) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setDictionaryName(type.getName());
					fieldDTO.clearValues();
					List<SimpleDictionary> dictDTOList = new ArrayList<>();
					for (String key : keys) {
						SimpleDictionary dto = dictionary().get(type, key);
						if (dto != null) {
							dictDTOList.add(dto);
						}
					}
					fieldDTO.setValues(dictDTOList);
				});
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link RowDependentFieldsDictionaryMeta#setDictionaryValues(DtoField, Collection)} instead
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void setDictionaryTypeWithCustomValues(DtoField<? super T, ?> field, String... keys) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearValues();
					List<SimpleDictionary> dictDTOList = new ArrayList<>();
					for (String key : keys) {
						SimpleDictionary dto = new SimpleDictionary(key, key);
						dictDTOList.add(dto);
					}
					fieldDTO.setValues(dictDTOList);
				});
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link RowDependentFieldsDictionaryMeta#setDictionaryValues(DtoField, Collection)} instead
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void setDictionaryTypeWithConcreteValues(DtoField<? super T, ?> field, IDictionaryType type,
			List<LOV> lovs) {
		String[] keys = lovs.stream()
				.filter(Objects::nonNull)
				.map(LOV::getKey)
				.toArray(String[]::new);
		setDictionaryTypeWithConcreteValues(field, type, keys);
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link RowDependentFieldsDictionaryMeta#setDictionaryValues(DtoField, Collection)} instead
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void setDictionaryTypeWithConcreteValues(DtoField<? super T, ?> field, IDictionaryType type,
			LOV... lovs) {
		String[] keys = Arrays.stream(lovs)
				.filter(Objects::nonNull)
				.map(LOV::getKey)
				.toArray(String[]::new);
		setDictionaryTypeWithConcreteValues(field, type, keys);
	}

}
