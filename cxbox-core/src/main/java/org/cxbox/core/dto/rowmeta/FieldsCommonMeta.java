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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.dictionary.DictionaryProvider;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class FieldsCommonMeta<T extends DataResponseDTO> extends RowDependentFieldsMeta<T> {

	public FieldsCommonMeta(@Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objectMapper, Optional<DictionaryProvider> dictionaryProvider) {
		super(objectMapper, dictionaryProvider);
	}

	@SafeVarargs
	public final void enableFilter(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
								dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setFilterable(true)));
	}

	/**
	 * Sets field filter options.
	 * Actually this method can be used to restrict filter options for non-dictionary fields too (e.g. input and so on. See compatible field types in documentation)
	 */
	public final void setConcreteFilterValues(DtoField<? super T, ?> field, Collection<SimpleDictionary> dictDtoList) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearFilterValues();
					fieldDTO.setFilterValues(dictDtoList);
				});
	}

	/**
	 * @deprecated Since 4.0.0-M12 use {@link FieldsMeta#setConcreteFilterValues(DtoField, Collection)} instead
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void addConcreteFilterValue(DtoField<? super T, ?> field, SimpleDictionary dictDTO) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> fieldDTO.addFilterValue(dictDTO));
	}

	@SafeVarargs
	public final void setForceActive(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
								dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setForceActive(true)));

	}

	@SafeVarargs
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public final void setEphemeral(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
								dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setEphemeral(true)));
	}

	public final void setFileAccept(DtoField<? super T, ?> field, @NonNull List<String> accept) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setFileAccept(null);
					fieldDTO.setFileAccept(String.join(",", accept));
				});
	}

	/**
	 * @param fields  fields to be made <code>sortable</code>. Sort icon will appear in UI, that user can interact with to apply/change sorting order
	 * <ul>
	 *     <li>See additional abilities for sorting  (how to set <code>default sort order</code> and so on) in this java doc
	 *     {@link org.cxbox.core.config.properties.WidgetFieldsIdResolverProperties#sortEnabledDefault}</li>
	 * </ul>
	 */
	@SafeVarargs
	public final void enableSort(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
								dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setSortable(true)));
	}

}
