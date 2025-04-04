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

package org.cxbox.api.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.MethodUtils;

@UtilityClass
public final class CxReflectionUtils {

	/**
	 * Gets all non-synthetic fields of the given class and its parents (if any).
	 * Frameworks like jacoco add synthetic fields for internal usage.
	 * This method can be used instead of FieldUtils.getAllFieldsList to avoid clashes with such fields
	 *
	 * @param cls the {@link Class} to query
	 * @return an array of Fields (possibly empty).
	 * @throws IllegalArgumentException if the class is {@code null}
	 */
	@SuppressWarnings("java:S3011")
	public static List<Field> getAllNonSyntheticFieldsList(final Class<?> cls) {
		List<Field> fields = FieldUtils.getAllFieldsList(cls)
				.stream()
				.filter(field -> !field.isSynthetic())
				.toList();
			List<Field> result = fields.stream().filter(AccessibleObject::trySetAccessible).collect(Collectors.toList());
			result.forEach(f ->  f.setAccessible(true));
			return result;
	}

	public static List<Field> getFields(final Class<?> cls) {
		return getAllNonSyntheticFieldsList(cls);
	}

	@SneakyThrows
	public static Class<?> forName(final String cls) {
		return Class.forName(cls);
	}

	public static Field findField(Class<?> dtoClazz, String name) {
		return FieldUtils.getField(dtoClazz, name, true);
	}

	public static Method getMatchingMethod(final Class<?> cls, final String methodName,
			final Class<?>... parameterTypes) {
		return MethodUtils.getMatchingMethod(cls, methodName, parameterTypes);
	}

	public static <T> Constructor<T> getMatchingAccessibleConstructor(final Class<T> cls,
			final Class<?>... parameterTypes) {
		return ConstructorUtils.getMatchingAccessibleConstructor(cls, parameterTypes);
	}

}
