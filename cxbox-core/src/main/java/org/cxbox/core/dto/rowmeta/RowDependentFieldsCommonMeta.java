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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.DataResponseDTO.OperationType;
import org.cxbox.api.data.dto.DataResponseDTO_;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.api.data.dto.rowmeta.FieldsDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.FieldDrillDown;
import org.cxbox.core.service.action.DrillDownTypeSpecifier;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class RowDependentFieldsCommonMeta<T extends DataResponseDTO> extends FieldsDTO {

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	protected final ObjectMapper objectMapper;

	public RowDependentFieldsCommonMeta(@Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public FieldDTO get(final DtoField<? super T, ?> field) {
		return fields.get(field.getName());
	}

	/**
	 * <br>
	 * @param field  field ref
	 * @return  currentValue of field. Optional.empty() if value is null or field is not present.
	 * @param <F>  field type
	 */
	@NonNull
	public <F> Optional<F> getCurrentValue(@NonNull final DtoField<? super T, F> field) {
		if (field.getValueClazz() != null) {
			return Optional.ofNullable(this.get(field))
					.filter(e -> e.getCurrentValue() != null)
					.filter(e -> field.getValueClazz().isInstance(e.getCurrentValue()))
					.map(e -> field.getValueClazz().cast(e.getCurrentValue()));
		} else {
			return Optional.ofNullable(this.get(field))
					.filter(e -> e.getCurrentValue() != null)
					.map(e -> {
						try {
							return (F) e.getCurrentValue();
						} catch (ClassCastException ignored) {
							//skip
						}
						return null;
					});
		}
	}

	/**
	 * Adds a value to an existing list of selectable values
	 *
	 * @param field widget field with type dictionary
	 * @param dictDTO DTO with dictionary value
	 */
	public final void addConcreteValue(DtoField<? super T, ?> field, SimpleDictionary dictDTO) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> fieldDTO.addValue(dictDTO));
	}

	@SafeVarargs
	public final void setRequired(DtoField<? super T, ?>... fields) {
		required(true, fields);
	}

	@SafeVarargs
	public final void setNotRequired(DtoField<? super T, ?>... fields) {
		required(false, fields);
	}

	@SafeVarargs
	public final void required(boolean required, DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(field ->
				Optional.ofNullable(field)
						.map(dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setRequired(required)));
	}

	@SafeVarargs
	public final void setHidden(DtoField<? super T, ?>... fields) {
		hidden(true, fields);
	}

	@SafeVarargs
	public final void setNotHidden(DtoField<? super T, ?>... fields) {
		hidden(false, fields);
	}

	@SafeVarargs
	public final void hidden(boolean required, DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(field ->
				Optional.ofNullable(field)
						.map(dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setHidden(required)));
	}

	public final void disableFields() {
		fields.values().forEach(fieldDTO -> fieldDTO.setDisabled(true));
	}

	@SafeVarargs
	public final void setDisabled(DtoField<? super T, ?>... fields) {
		disable(true, fields);
	}

	@SafeVarargs
	public final void setEnabled(DtoField<? super T, ?>... fields) {
		disable(false, fields);
	}

	public final void disable(final boolean disabled, DtoField<? super T, ?>... fields) {
		List<String> fieldsList = Stream.of(fields).map(DtoField::getName).collect(Collectors.toList());
		disable(disabled, fieldsList);
	}

	public void setDisabled(List<String> fields) {
		disable(true, fields);
	}

	private final void disable(final boolean disabled, List<String> fields) {
		fields.forEach(field -> Optional.ofNullable(field).map(dtoField -> this.fields.get(dtoField))
				.ifPresent(fieldDTO -> fieldDTO.setDisabled(disabled)));
	}

	public final void setConcreteValues(DtoField<? super T, ?> field, Collection<SimpleDictionary> dictDtoList) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setDictionaryName(field.getName());
					fieldDTO.clearValues();
					fieldDTO.setValues(dictDtoList);
				});
	}

	public final void setDrilldown(DtoField<? super T, ?> field, DrillDownTypeSpecifier drillDownType, String drillDown) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setDrillDown(drillDown);
					fieldDTO.setDrillDownType(drillDownType.getValue());
				});
	}

	public final void setDrilldowns(final List<FieldDrillDown> drillDowns) {
		for (final FieldDrillDown drillDown : drillDowns) {
			Optional.ofNullable(drillDown).map(dd -> fields.get(dd.getTaskField()))
					.ifPresent(fieldDTO -> {
						fieldDTO.setDrillDown(drillDown.getUrl());
						fieldDTO.setDrillDownType(drillDown.getType().getValue());
					});
		}
	}

	public final <V> void setCurrentValue(DtoField<? super T, V> field, V value) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> fieldDTO.setCurrentValue(value));
	}

	public final void setPlaceholder(DtoField<? super T, ?> field, String placeholder) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> fieldDTO.setPlaceholder(placeholder));
	}

	/**
	 * Сhecks whether the specified field was modified by the user in the UI (Frontend) during the current iteration.
	 *
	 * @param fields Current state of field metadata.
	 * @param field DTO field to check
	 * @return boolean. true – If the field was changed in the UI during the current iteration. false – If the field remains unchanged.
	 */
	public <V> boolean isFieldChangedNowFE(RowDependentFieldsMeta<T> fields,
			DtoField<? super T, V> field) {
		return fields.getCurrentValue(DataResponseDTO_.changedNowParam)
				.map(objectMap -> objectMap.getChangedNow().contains(field.getName()))
				.orElse(false);
	}

	/**
	 * Checks if the field was changed in the UI during /data operation.
	 *
	 * @param fields Current state of field metadata
	 * @param field DTO field to check
	 * @param <V> Type of the field value
	 * @return true if the field was changed in the UI during data operation, false otherwise
	 */
	public <V> boolean isFieldChangedNowFEForData(RowDependentFieldsMeta<T> fields,
			DtoField<? super T, V> field) {
		return checkFieldChangedForOperation(fields, field, OperationType.DATA);
	}

	/**
	 * Checks if the field was changed in the UI during /row-meta operation.
	 *
	 * @param fields Current state of field metadata
	 * @param field DTO field to check
	 * @param <V> Type of the field value
	 * @return true if the field was changed in the UI during metadata operation, false otherwise
	 */
	public <V> boolean isFieldChangedNowFEForRowMeta(RowDependentFieldsMeta<T> fields,
			DtoField<? super T, V> field) {
		return checkFieldChangedForOperation(fields, field, OperationType.META);
	}

	/**
	 * Checks if the field was changed in the UI during standard action operation.
	 *
	 * @param fields Current state of field metadata
	 * @param field DTO field to check
	 * @param <V> Type of the field value
	 * @return true if the field was changed in the UI during action operation, false otherwise
	 */
	public <V> boolean isFieldChangedNowFEForAction(RowDependentFieldsMeta<T> fields,
			DtoField<? super T, V> field) {
		return checkFieldChangedForOperation(fields, field, OperationType.ACTION);
	}

	/**
	 * Checks if the field was changed in the UI during a specific custom action.
	 *
	 * @param fields Current state of field metadata
	 * @param field DTO field to check
	 * @param actionName Name of the action
	 * @param <V> Type of the field value
	 * @return true if the field was changed in the UI during the specified action, false otherwise
	 */
	public <V> boolean isFieldChangedNowFEForCustomAction(RowDependentFieldsMeta<T> fields,
			DtoField<? super T, V> field, String actionName) {
		return checkFieldChangedForOperationAndAction(fields, field, OperationType.ACTION, actionName);
	}

	/**
	 * method to check field change for a specific operation type.
	 */
	private <V> boolean checkFieldChangedForOperation(RowDependentFieldsMeta<T> fields,
			DtoField<? super T, V> field, OperationType operationType) {
		return fields.getCurrentValue(DataResponseDTO_.changedNowParam)
				.map(objectMap -> objectMap.getChangedNow().contains(field.getName())
						&& objectMap.getOperationType().equals(operationType))
				.orElse(false);
	}

	/**
	 * method to check field change for a specific operation type and action.
	 */
	private <V> boolean checkFieldChangedForOperationAndAction(RowDependentFieldsMeta<T> fields,
			DtoField<? super T, V> field, OperationType operationType, String actionName) {
		return fields.getCurrentValue(DataResponseDTO_.changedNowParam)
				.map(objectMap -> objectMap.getChangedNow().contains(field.getName())
						&& objectMap.getOperationType().equals(operationType)
						&& objectMap.getActionNameOperationType().equals(actionName))
				.orElse(false);
	}

	public <F> Optional<F> getCurrentValueChangedNowFE(RowDependentFieldsMeta<T> fields,
			final DtoField<? super T, F> field) {
		DataResponseDTO dto = fields.getCurrentValue(DataResponseDTO_.changedNowParam).get().getChangedNowDTO();
		return (Optional<F>) getFieldValue(dto, field.getName());
	}

	@SneakyThrows
	private Object getFieldValue(Object dto, String fieldName) {
		Field field = dto.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(dto);
	}

}
