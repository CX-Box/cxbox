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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckChangeNowService {

	private static final String CHANGED_NOW = "changedNow";

	public boolean isChangedNowData(Map<String, Object> dataFE) {
		Map<String, Object> dataChangedFE = (Map<String, Object>) dataFE.get(CHANGED_NOW);
		return dataChangedFE != null && !dataChangedFE.isEmpty();
	}

	public void validateChangedNowFields(Map<String, ?> changedNow,
			DataResponseDTO changedNowDTO,
			DataResponseDTO requestDto) {
		changedNow.keySet().forEach(key -> {
			Object newValue = getFieldValue(changedNowDTO, key);
			Object oldValue = getFieldValue(requestDto, key);

			if (!areEqual(newValue, oldValue)) {
				log.error("Field \"" + key + "\" has different values: "
						+ newValue + " != " + oldValue);
			}
		});
	}

	private boolean areEqual(Object newValue, Object oldValue) {
		if (Objects.equals(newValue, oldValue)) {
			return true;
		}

		if (newValue instanceof Collection && oldValue instanceof Collection) {
			Collection<?> newCol = (Collection<?>) newValue;
			Collection<?> oldCol = (Collection<?>) oldValue;

			return new HashSet<>(newCol).equals(new HashSet<>(oldCol));
		}

		if (newValue instanceof Object[] && oldValue instanceof Object[]) {
			return Arrays.equals((Object[]) newValue, (Object[]) oldValue);
		}

		return false;
	}

	@SneakyThrows
	public Object getFieldValue(Object dto, String fieldName) {
		Field field = dto.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(dto);
	}

}
