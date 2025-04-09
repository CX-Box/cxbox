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

package org.cxbox.api.data.dto;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.cxbox.api.data.IDataContainer;
import org.cxbox.constgen.DtoField;
import org.cxbox.constgen.DtoMetamodelIgnore;
import org.cxbox.constgen.GeneratesDtoMetamodel;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Getter
@Setter
@GeneratesDtoMetamodel
@JsonFilter("dtoPropertyFilter")
public abstract class DataResponseDTO implements CheckedDto, IDataContainer<DataResponseDTO>, Serializable {

	@JsonIgnore
	@DtoMetamodelIgnore
	protected Set<String> changedFields = new TreeSet<>();

	protected List<Steps> steps = new ArrayList<>();

	protected long step = 0;

	protected String id;

	protected Entity errors;

	@Ephemeral
	protected long vstamp;

	@JsonIgnore
	@DtoMetamodelIgnore
	private Set<String> serializableFields;

	@JsonIgnore
	@DtoMetamodelIgnore
	private Set<String> computedFields;

	public boolean hasChangedFields() {
		return changedFields.size() > 0;
	}

	public boolean isFieldChanged(final DtoField<?, ?> dtoField) {
		return isFieldChanged(dtoField.getName());
	}

	public boolean isFieldChanged(final String fieldName) {
		return changedFields.contains(fieldName);
	}

	public  <V, T extends DataResponseDTO> void addStepsList(final DtoField<? super T, V> dtoField, V value) {
		Optional<Steps> fieldChangedList =
				steps.stream().filter(map -> step == map.getStep()).findFirst();
		fieldChangedList.ifPresent(v -> v.addDataBE(dtoField,value));
	}

	public boolean isFieldChangedNow(final DtoField<?, ?> dtoField) {
		return isFieldChangedNow(dtoField.getName(), ChangedByCode.FRONTEND_AND_BACKEND);
	}

	public boolean isFieldChangedNowByBE(final DtoField<?, ?> dtoField) {
		return isFieldChangedNow(dtoField.getName(), ChangedByCode.BACKEND);
	}

	public boolean isFieldChangedNowByFE(final DtoField<?, ?> dtoField) {
		return isFieldChangedNow(dtoField.getName(), ChangedByCode.FRONTEND);
	}

	public boolean isFieldChangedNow(final String fieldName, final ChangedByCode changedBy) {
		return steps.stream()
				.filter(map -> step == map.getStep())
				.findFirst()
				.map(value -> {
					return switch (changedBy) {
						case FRONTEND -> value.getDataFE().containsKey(fieldName);
						case BACKEND -> value.getDataBE().containsKey(fieldName);
						default -> value.getDataBE().containsKey(fieldName) ||
								value.getDataFE().containsKey(fieldName);
					};
				})
				.orElse(false);
	}

	public void addChangedField(String fieldName) {
		changedFields.add(fieldName);
	}

	public void addChangedField(DtoField<?, ?> dtoField) {
		addChangedField(dtoField.getName());
	}

	public boolean isFieldSerializable(String fieldName) {
		return serializableFields == null || serializableFields.contains(fieldName);
	}

	public boolean isFieldComputed(String fieldName) {
		return computedFields == null || computedFields.contains(fieldName);
	}

	public void addComputedField(String fieldName) {
		if (computedFields == null) {
			computedFields = new TreeSet<>();
		}
		computedFields.add(fieldName);
	}

	@Override
	public void transformData(Function<DataResponseDTO, DataResponseDTO> function) {
		function.apply(this);
	}

	@Setter
	@Getter
	public static class Steps {

		private long step;

		private Map<String, Object> dataBE = new HashMap<>();

		private Map<String, Object> dataFE = new HashMap<>();

		public <V, T extends DataResponseDTO> void addDataBE(DtoField<? super T, V> dtoFieldChange, V value) {
			dataBE.put(dtoFieldChange.getName(),value);
		}

	}




}
