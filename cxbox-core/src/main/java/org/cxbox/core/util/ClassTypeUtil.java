package org.cxbox.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ClassTypeUtil {

	public static Class<?> getGenericType(Class<?> clazz, int index) {
		Type superclass = clazz.getGenericSuperclass();

		if (superclass instanceof ParameterizedType parameterizedType) {
			Type actualType = parameterizedType.getActualTypeArguments()[index];

			if (actualType instanceof Class<?> actualClass) {
				return actualClass;
			} else {
				throw new IllegalArgumentException("Тип дженерика не является классом");
			}
		}

		throw new IllegalArgumentException("Не удалось получить тип дженерика");
	}

}
