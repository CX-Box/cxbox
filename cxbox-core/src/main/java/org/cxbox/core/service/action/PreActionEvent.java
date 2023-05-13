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

package org.cxbox.core.service.action;

import org.cxbox.api.data.dictionary.CoreDictionaries.PreInvokeType;
import org.cxbox.api.data.dictionary.LOV;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PreActionEvent {

	private final String key;

	private final LOV type;

	private final String message;

	private final PreActionCondition preActionCondition;

	public static PreActionEvent of(String key, LOV type, PreActionCondition preActionCondition, String message) {
		return PreActionEvent.builder()
				.key(key)
				.type(type)
				.preActionCondition(preActionCondition)
				.message(message)
				.build();
	}

	public static PreActionEvent confirm(String key, PreActionCondition preActionCondition, String message) {
		return PreActionEvent.of(key, PreInvokeType.CONFIRMATION, preActionCondition, message);
	}

	public static PreActionEvent info(String key, PreActionCondition preActionCondition, String message) {
		return PreActionEvent.of(key, PreInvokeType.INFORMATION, preActionCondition, message);
	}

	public static PreActionEvent error(String key, PreActionCondition preActionCondition, String message) {
		return PreActionEvent.of(key, PreInvokeType.ERROR, preActionCondition, message);
	}

}
