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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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

	private final Boolean success;

	private final String errorMessage;

	/**
	 * Values of the row that the frontend sends with the mass action, for example the files of a mass signing.
	 * <p>
	 * Platform keys are in {@link MassOptionType}. A project adds its own keys with an enum
	 * that implements {@link MassOptionTypeSpecifier}. The core accepts any key and does not check it.
	 * <p>
	 * The action result has {@code options} only when a value was added with {@link #addOption}.
	 */
	@JsonProperty
	@JsonSetter(nulls = Nulls.AS_EMPTY)
	@JsonInclude(Include.NON_EMPTY)
	private Map<String, String> options = new HashMap<>();

	public static MassDTO success(@NonNull String id) {
		return new MassDTO(id, true, null);
	}

	public static MassDTO fail(@NonNull String id) {
		return new MassDTO(id, false, null);
	}

	public static MassDTO fail(@NonNull String id, @NonNull String errorMessage) {
		return new MassDTO(id, false, errorMessage);
	}

	/**
	 * @return the value that the frontend sent for this row under the key, or {@code null} if there is none
	 */
	public String getOption(@NonNull MassOptionTypeSpecifier key) {
		return options.get(key.getValue());
	}

	public MassDTO addOption(@NonNull MassOptionTypeSpecifier key, String value) {
		options.put(key.getValue(), value);
		return this;
	}

	public MassDTO deleteOption(@NonNull MassOptionTypeSpecifier key) {
		options.remove(key.getValue());
		return this;
	}
}
