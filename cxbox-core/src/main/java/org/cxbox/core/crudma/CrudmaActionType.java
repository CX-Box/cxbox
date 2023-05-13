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

package org.cxbox.core.crudma;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum CrudmaActionType {

	INVOKE(true, false),
	FIND(false, true),
	GET(false, true),
	UPDATE(true, false),
	PREVIEW(true, true),
	DELETE(false, false),
	COUNT(false, true),
	ASSOCIATE(false, false),
	META(false, true),
	CREATE(true, true);

	private final boolean flushRequired;

	private final boolean readOnly;

}
