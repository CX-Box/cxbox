package org.cxbox.core.util.filter.drilldowns;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import lombok.Getter;

@Getter
public abstract class TypeToken<T> {

	private final Type type;

	protected TypeToken() {
		Type superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class) {
			throw new RuntimeException("Missing type parameter.");
		}
		this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	public T newInstance() {
		try {
			Class<?> rawType = type instanceof Class
					? (Class<?>) type
					: (Class<?>) ((ParameterizedType) type).getRawType();
			return (T) rawType.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
