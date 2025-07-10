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

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class MassDTO implements CheckedDto, Serializable {

	@NonNull
	private final String id;

	@NonNull
	private final Boolean success;

	private final String errorMessage;

	public static MassDTO success(@NonNull String id) {
		return new MassDTO(id, true, null);
	}

	public static MassDTO fail(@NonNull String id) {
		return new MassDTO(id, false, null);
	}

	public static MassDTO fail(@NonNull String id, @NonNull String errorMessage) {
		return new MassDTO(id, false, errorMessage);
	}
}
