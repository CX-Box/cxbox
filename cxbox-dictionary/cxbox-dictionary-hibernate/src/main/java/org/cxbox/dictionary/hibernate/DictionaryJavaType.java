/*
 * © OOO "SI IKS LAB", 2022-2026
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

package org.cxbox.dictionary.hibernate;

import org.cxbox.dictionary.Dictionary;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;

/**
 * JavaType for Dicitionary
 * <a href="https://docs.hibernate.org/orm/6.6/userguide/html_single/#basic-legacy">doc</a>
 */
public class DictionaryJavaType<T extends Dictionary>
		extends AbstractClassJavaType<T> {

	public DictionaryJavaType(Class<T> clazz) {
		super(clazz, ImmutableMutabilityPlan.instance());
	}

	@Override
	public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
		return indicators.getTypeConfiguration()
				.getJdbcTypeRegistry()
				.getDescriptor(SqlTypes.VARCHAR);
	}

	@Override
	public T fromString(CharSequence key) {
		if (key == null) {
			return null;
		}
		return Dictionary.of(getJavaTypeClass(), key.toString());
	}

	@Override
	public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
		if (value == null) {
			return null;
		}
		if (type.isInstance(value)) {
			return (X) value;
		}
		if (type.isAssignableFrom(String.class)) {
			return type.cast(value.key());
		}
		throw unknownUnwrap(type);
	}

	@Override
	public <X> T wrap(X value, WrapperOptions options) {
		if (value == null) {
			return null;
		}
		var javaTypeClass = getJavaTypeClass();
		if (javaTypeClass.isInstance(value)) {
			return (T) value;
		}
		if (value instanceof String s) {
			return fromString(s);
		}
		throw unknownWrap(value.getClass());
	}

}