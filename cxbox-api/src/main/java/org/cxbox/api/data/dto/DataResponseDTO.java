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
import java.util.HashMap;
import java.util.Map;
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

	/**
	 *  Field changes received from the frontend.
	 * <p>
	 * Transfers data from {@code Map<String, Object> changedNow}
	 */
	@JsonIgnore
	private ChangedNowParam changedNowParam;

	/**
	 * Fields currently being updated by the frontend
	 *
	 * <p>Each entry consists of:
	 * <ul>
	 *   <li><b>Key</b> - The name of the field being modified</li>
	 *   <li><b>Value</b> - The new value to be set for the field (Object -> DataResponseDTO) </li>
	 * </ul>
	 *
	 * <p>Note: This map is for transient tracking purposes only and should not be persisted.
	 */
	@JsonIgnore
	@DtoMetamodelIgnore
	private Map<String, Object> changedNow_ = new HashMap<>();

	public boolean hasChangedFields() {
		return changedFields.size() > 0;
	}

	public boolean isFieldChanged(final DtoField<?, ?> dtoField) {
		return isFieldChanged(dtoField.getName());
	}

	public boolean isFieldChanged(final String fieldName) {
		return changedFields.contains(fieldName);
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

	@Getter
	@Setter
	@SuperBuilder
	public static class ChangedNowParam {

		/**
		 * Set of field names that are currently being modified.
		 * <p>
		 * Contains only the names of changed fields.
		 *
		 * <p>
		 * Unlike {@code changedFields}, which contains the names of all fields changed
		 * throughout all iterations of editing the entity, this set ({@code changedNow})
		 * includes only the fields that were modified during the current operation.
		 */
		private Set<String> changedNow;

		/**
		 * Containing the actual new values for changed fields.
		 * <p>
		 * Each field in this DTO corresponds to a field name in {@code changedNow} with  value
		 */
		private DataResponseDTO changedNowDTO;

	}

}
