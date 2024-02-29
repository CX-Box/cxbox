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

package org.cxbox.core.crudma.bc.impl;

import org.cxbox.core.crudma.Crudma;
import org.cxbox.api.data.BcIdentifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public abstract class BcDescription implements BcIdentifier {

	private final String name;

	private final String parentName;

	private final Class<? extends Crudma> crudmaService;

	/**
	 * Prohibition of caching BC by the front
	 */
	private final boolean refresh;

	//TODO used only for SqlBC. Delete after refactoring
	protected Long id;

	//TODO used only for SqlBC. Delete after refactoring
	protected String bindsString;

	//TODO used only for SqlBC. Delete after refactoring
	protected Long pageLimit;
}
