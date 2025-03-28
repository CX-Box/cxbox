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

import static java.lang.String.format;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PreActionType {

	CONFIRMATION("confirm", "Подтвердить действие '%s'?"),
	INFORMATION("info", "Выполняется действие '%s'"),
	ERROR("error", "Действие '%s' не может быть выполнено"),
	CUSTOM("custom", null);

	private final String type;

	private final String message;

	public String getMessage(String... actionName) {
		return message != null ? format(message, (Object[]) actionName) : message;
	}

}
