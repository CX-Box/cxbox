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
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.dictionary.Dictionary;
import org.cxbox.dictionary.DictionaryProvider;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class RowDependentFieldsDictionaryMeta<T extends DataResponseDTO> extends
		RowDependentFieldsDictionaryDeprecatedMeta<T> {

	private final Optional<DictionaryProvider> dictionaryProvider;

	public RowDependentFieldsDictionaryMeta(@Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objectMapper,
			Optional<DictionaryProvider> dictionaryProvider) {
		super(objectMapper);
		this.dictionaryProvider = dictionaryProvider;
	}

	/**
	 * <br>
	 * <br>
	 *
	 * @param field - widget dictionary field
	 * @param variants - variants to be shown in field drop-down (Form widget field during editing and so on)
	 * @param <V> - dictionary class. Usually class name determines dictionary type, that is used to convert keys to values for UI (see {@link Dictionary#getDictionaryType()})
	 */
	public final <V extends Dictionary> void setDictionaryValues(@Nullable DtoField<? super T, V> field,
			@NonNull Collection<V> variants) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setDictionaryName(Dictionary.of(field.getValueClazz(), "").getDictionaryType());
					List<SimpleDictionary> dictDTOList = mapDictionary(variants);
					fieldDTO.clearValues();
					fieldDTO.setValues(dictDTOList);
				});
	}

	/**
	 * <br>
	 * <br>
	 * For internal usage only
	 */
	<V extends Dictionary> List<SimpleDictionary> mapDictionary(Collection<V> variants) {
		List<SimpleDictionary> dictDTOList = new ArrayList<>();
		for (Dictionary variant : variants) {
			dictionaryProvider.ifPresent(p -> {
				var dict = p.lookupValue(variant);
				if (dict != null) {
					if (dict instanceof SimpleDictionary simpleValue) {
						dictDTOList.add(simpleValue);
					} else {
						dictDTOList.add(new SimpleDictionary(variant.key(), dict.getValue()));
					}
				}
			});
		}
		return dictDTOList;
	}

	/**
	 * <br>
	 * <br>
	 * Fills dictionary field drop-down (Form widget field during editing and so on) with all values from {@link org.cxbox.dictionary.Dictionary#getDictionaryType() dictionary type}
	 *
	 * @param field - widget dictionary field
	 * @param <V> - dictionary class. Usually class name determines dictionary type, that is used to determine dictionary values and to convert keys to values for UI (see {@link Dictionary#getDictionaryType()})
	 * @implNote If you use {@link DictionaryProvider#getAll(Class)}, that delegates to {@link org.cxbox.api.data.dictionary.DictionaryCache}, then drop-down values are sorted by display_order, then by key (display_order can be null)
	 * <p>
	 * See {@code dicts.sort} line in {@link org.cxbox.model.core.service.DictionaryCacheImpl.Cache#load()}
	 * <p>
	 * <br>
	 * Attention - sorting rows in List widgets always ignores display_order and is done by key lexicographically!
	 */
	public final <V extends Dictionary> void setDictionaryValues(@Nullable DtoField<? super T, V> field) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					Collection<V> variants = dictionaryProvider.map(e -> e.getAll(field.getValueClazz()))
							.orElse(new ArrayList<>());
					setDictionaryValues(field, variants);
				});
	}

	/**
	 * <br>
	 * <br>
	 * Fills dictionary field drop-down (Form widget field during editing and so on).
	 */
	@SafeVarargs
	public final <E extends Enum<?>> void setEnumValues(
			@Nullable DtoField<? super T, E> field,
			@NonNull E... values) {
		if (field != null) {
			this.setConcreteValues(field, Arrays
					.stream(values)
					.map(en -> new SimpleDictionary(en.name(), serialize(en)))
					.collect(Collectors.toList())
			);
		}
	}

	@SneakyThrows
	String serialize(@NonNull Enum<?> en) {
		final String serialize = objectMapper.writeValueAsString(en);
		return serialize.substring(1, serialize.length() - 1);
	}

}
