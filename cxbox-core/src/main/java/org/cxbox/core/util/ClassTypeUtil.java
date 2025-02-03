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
				throw new IllegalArgumentException("The generic type is not a class");
			}
		}

		throw new IllegalArgumentException("Couldn't get the generic type");
	}

}
