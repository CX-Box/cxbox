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

package org.cxbox.constgen;

import java.io.Serializable;
import java.util.function.Function;

public final class DtoField<D, T> implements Serializable {

	public DtoField(final String name) {
		this.name = name;
		this.getter = noDefaultGetter -> {
			throw new DefaultGetterNotFoundException(this.name);
		};
	}

	private final String name;

	private final Function<D, T> getter;


	public DtoField(String name, Function<D, T> getter) {
		this.name = name;
		this.getter = getter;
	}

	public T getValue(D dto) {
		return this.getter.apply(dto);
	}

	public String getName() {
		return this.name;
	}

	public Function<D, T> getGetter() {
		return this.getter;
	}

	private static class DefaultGetterNotFoundException extends RuntimeException {

		public DefaultGetterNotFoundException(final String fieldName) {
			super("DTO hasn't default getter for field: " + fieldName);
		}

	}

}
