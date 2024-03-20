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
import java.util.List;
import org.cxbox.api.data.dictionary.IDictionaryType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.IconCode;
import org.cxbox.constgen.DtoField;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class FieldsMeta<T extends DataResponseDTO> extends RowDependentFieldsMeta<T> {

	public FieldsMeta(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	/**
	 * Adds a value to the existing list of filterable values
	 *
	 * @param field widget field with type dictionary
	 * @param dictDTO DTO with dictionary value
	 */
	public final void addConcreteFilterValue(DtoField<? super T, ?> field, SimpleDictionary dictDTO) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> fieldDTO.addFilterValue(dictDTO));
	}

	//DtoField METHODS
	@SafeVarargs
	public final void enableFilter(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
						dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setFilterable(true)));
	}

	public final void setAllFilterValuesByLovType(DtoField<? super T, ?> field, IDictionaryType type) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearFilterValues();
					fieldDTO.setFilterValues(dictionary().getAll(type));
				});
	}

	public final void setConcreteFilterValues(DtoField<? super T, ?> field, Collection<SimpleDictionary> dictDtoList) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearFilterValues();
					fieldDTO.setFilterValues(dictDtoList);
				});
	}


	public <T extends DataResponseDTO, E extends Enum> void setEnumFilterValues(
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

	@SafeVarargs
	public final void setForceActive(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
						dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setForceActive(true)));

	}

	@SafeVarargs
	public final void setEphemeral(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
						dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setEphemeral(true)));
	}

	@SafeVarargs
	public final void setHidden(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
						dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setHidden(true)));
	}

	public final void setFilterValuesWithIcons(DtoField<? super T, ?> field, IDictionaryType type,
			Map<LOV, IconCode> valueIconMap) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setDictionaryName(type.getName());
					fieldDTO.clearValues();
					valueIconMap
							.forEach((key, value) -> fieldDTO
									.setIconWithValue(type.lookupValue(key), value, true));
				});
	}

	public final void setFileAccept(DtoField<? super T, ?> field, @NonNull List<String> accept) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setFileAccept(null);
					fieldDTO.setFileAccept(String.join(",", accept));
				});
	}

}
