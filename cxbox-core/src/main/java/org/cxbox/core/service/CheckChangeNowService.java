/*
 * © OOO "SI IKS LAB", 2022-2024
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

package org.cxbox.core.service;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.FieldsDTO;
import org.cxbox.api.data.dto.rowmeta.PreviewResult;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.PlatformRequest;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.multivalue.MultivalueField;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.service.rowmeta.RowResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.util.FieldUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckChangeNowService {

	@Autowired
	private PlatformRequest platformRequest;

	@Lazy
	@Autowired
	private RowResponseService rowResponseService;

	/**
	 * Checks whether the input data contains fields that have been changed in the current request.
	 * <p>
	 * This method looks for a nested map under the {@code CHANGED_NOW} key and returns {@code true}
	 * if it exists and is not empty.
	 * </p>
	 *
	 * @param dataFE the input data from the frontend, which may contain changed fields
	 * @return {@code true} if there are changed fields under the {@code CHANGED_NOW} key; {@code false} otherwise
	 */
	public boolean isChangedNowData(Map<String, Object> dataFE) {
		Map<String, Object> dataChangedFE = (Map<String, Object>) dataFE.get("changedNow");
		return dataChangedFE != null && !dataChangedFE.isEmpty();
	}

	/**
	 * Performs entity update and reloads metadata to obtain up-to-date values of dependent fields.
	 * <p>
	 * If the input data contains modified fields (under the {@code CHANGED_NOW} key),
	 * a preview update is performed and metadata is reloaded. The method returns a map
	 * with updated values only for the fields that are visible in the UI.
	 * </p>
	 *
	 * @param bc the business component to which the updated entity belongs
	 * @param dataFE input data from the frontend containing field values and possible changes
	 * @param previewFunction function for performing a preview update of the entity
	 * (equivalent to calling {@code preview(bc, dataFE)})
	 * @param onFieldUpdateMetaFunction function for retrieving metadata after field changes
	 * (equivalent to calling {@code getOnFieldUpdateMeta(bc, dto)})
	 * @return a map with updated field values, including only visible and dependent fields;
	 * if there were no changes, the original {@code dataFE} is returned
	 * @throws NullPointerException if any of the provided parameters is {@code null}
	 */

	public Map<String, Object> callDoUpdateAndReloadMeta(@NotNull BusinessComponent bc,
			@NotNull Map<String, Object> dataFE,
			@NotNull BiFunction<BusinessComponent, Map<String, Object>, PreviewResult> previewFunction,
			@NotNull BiFunction<BusinessComponent, DataResponseDTO, MetaDTO> onFieldUpdateMetaFunction) {

		Map<String, Object> data;

		if (isChangedNowData(dataFE)) {
			data = new HashMap<>();

			PreviewResult previewResult = previewFunction.apply(bc, dataFE);
			DataResponseDTO dto = previewResult.getResponseDto();

			if (CrudmaActionType.UPDATE.equals(platformRequest.getCrudmaActionType())
					&& previewResult.getRequestDto().getVstamp() != -1) {
				dto.setVstamp(dto.getVstamp() + 1);
			}

			MetaDTO metaDTO = onFieldUpdateMetaFunction.apply(bc, dto);
			FieldsDTO fieldsDTO = metaDTO.getRow().getFields();
			Set<String> allFields = rowResponseService.getAllFields(bc, dto);

			fieldsDTO.forEach(a -> data.put(a.getKey(), a.getCurrentValue()));
			fieldsDTO.forEach(a -> {
				if (!allFields.contains(a.getKey())) {
					data.remove(a.getKey());
				}
			});
		} else {
			data = dataFE;
		}

		return data;
	}

	/**
	 * Checks whether the data received in the changedNow tag from the frontend matches the values
	 * in the data tag.
	 *
	 * <p>
	 * This method compares the values of each field by name. For standard objects, it uses {@link Objects#equals(Object, Object)}.
	 * For fields of type {@link MultivalueField}, it verifies whether all values in the new object exist in the old object.
	 * <p>
	 * If a mismatch is detected, an error is logged indicating the differing values.
	 * No exception is thrown to avoid interrupting the application flow.
	 *
	 * @param changedNow     a map containing the names of fields to check (values in the map are ignored)
	 * @param changedNowDTO  an object representing the "new" state of the data
	 * @param requestDto     an object representing the "old" or original state of the data
	 */

	public void validateChangedNowFields(Map<String, ?> changedNow, DataResponseDTO changedNowDTO,
			DataResponseDTO requestDto) {
		changedNow.keySet().forEach(key -> {
			try {
				Object newValue = FieldUtils.getFieldValue(changedNowDTO, key);
				Object oldValue = FieldUtils.getFieldValue(requestDto, key);

				boolean isEqual = false;

				if (Objects.equals(newValue, oldValue)) {
					isEqual = true;
				} else if (newValue instanceof MultivalueField && oldValue instanceof MultivalueField) {
					MultivalueField newMV = (MultivalueField) newValue;
					MultivalueField oldMV = (MultivalueField) oldValue;

					isEqual = newMV.getValues().stream()
							.allMatch(newItem -> oldMV.getValues().stream()
									.anyMatch(oldItem -> Objects.equals(oldItem.getValue(), newItem.getValue())));
				}

				if (!isEqual) {
					log.error("Field \"{}\" has different values: {} != {}", key, newValue, oldValue);
				}

			} catch (IllegalAccessException e) {
				log.error("Error accessing field \"{}\": {}", key, e.getMessage(), e);
			}
		});
	}

}
