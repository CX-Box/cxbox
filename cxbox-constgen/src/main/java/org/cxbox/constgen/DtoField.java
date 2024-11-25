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
import lombok.NonNull;

public final class DtoField<D, T> implements Serializable {

	private final String name;

	private final Function<D, T> getter;

	private final Class<T> valueClazz;

	public DtoField(@NonNull String name, @NonNull Function<D, T> getter, Class<?> valueClazz) {
		this.name = name;
		this.getter = getter;
		this.valueClazz = (Class<T>) valueClazz;
	}

	/**
	 * @deprecated instead use type safe <code>{@link DtoField#DtoField(String, Function, Class)}</code>.
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public DtoField(@NonNull String name, Class<?> valueClazz) {
		this.name = name;
		this.getter = noDefaultGetter -> {
			throw new DefaultGetterNotFoundException(this.name);
		};
		this.valueClazz = (Class<T>) valueClazz;
	}

	public T getValue(D dto) {
		return this.getter.apply(dto);
	}

	@NonNull
	public String getName() {
		return this.name;
	}

	@NonNull
	public Function<D, T> getGetter() {
		return this.getter;
	}

	public Class<T> getValueClazz() {
		return this.valueClazz;
	}

	private static class DefaultGetterNotFoundException extends RuntimeException {

		public DefaultGetterNotFoundException(final String fieldName) {
			super("DTO hasn't default getter for field: " + fieldName);
		}

	}

}
