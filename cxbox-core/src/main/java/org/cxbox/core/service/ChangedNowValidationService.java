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

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.DataResponseDTO.ChangedNowParam;
import org.cxbox.core.dto.multivalue.MultivalueField;
import org.springframework.security.util.FieldUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangedNowValidationService {

	/**
	 * Checks whether the input data contains fields that have been changed in the current request.
	 * <p>
	 * This method looks for a nested map under the {@code CHANGED_NOW} key and returns {@code true}
	 * if it exists and is not empty.
	 * </p>
	 *
	 * @param data the input data from the frontend, which may contain changed fields
	 * @return {@code true} if there are changed fields under the {@code CHANGED_NOW} key; {@code false} otherwise
	 */
	public boolean isChangedNowData(DataResponseDTO data) {
		Map<String, Object> dataChangedFE =  data.getChangedNow_();
		return dataChangedFE != null && !dataChangedFE.isEmpty();
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

				if (!Objects.equals(newValue, oldValue)) {
						log.error("Field \"{}\" has different values: {} != {}", key, newValue, oldValue);
					}
			} catch (IllegalAccessException e) {
				log.error("Error accessing field \"{}\": {}", key, e.getMessage(), e);
			}
		});
	}

	/**
	 *
	 * <p>This method builds a new {@code CnangedNowParam}
	 *
	 * @param changedNowKeys a set of keys that represent the fields or values that have been changed
	 * @param changedNowDTO the data transfer object containing detailed information about the changes
	 * @return a fully constructed {@code CnangedNowParam} instance encapsulating the change data
	 */
	public ChangedNowParam buildCnangedNowParam(HashSet<String> changedNowKeys, DataResponseDTO changedNowDTO) {
		return ChangedNowParam.builder()
				.changedNowDTO(changedNowDTO)
				.changedNow(changedNowKeys)
				.build();
	}
}
