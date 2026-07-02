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

package org.cxbox.dictionary.hibernate;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import lombok.SneakyThrows;
import org.cxbox.dictionary.Dictionary;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.spi.TypeBootstrapContext;
import org.hibernate.usertype.DynamicParameterizedType;

/**
 * DynamicParameterizedType not planned removal for closest time
 * <a href="https://discourse.hibernate.org/t/alternative-to-dynamicparameterizedtype-entity-in-setparameters/11985">discource</a>
 */
@SuppressWarnings("removal")
public class DictionaryType extends ImmutableType<Dictionary> implements DynamicParameterizedType {

	private Class<? extends Dictionary> elementType;

	public DictionaryType(Class<? extends Dictionary> elementType) {
		super(Dictionary.class);
		this.elementType = elementType;
	}

	public DictionaryType() {
		super(Dictionary.class);
	}

	public DictionaryType(TypeBootstrapContext typeBootstrapContext) {
		super(Dictionary.class, typeBootstrapContext);
	}

	@Override
	public int getSqlType() {
		return Types.VARCHAR;
	}

	@Override
	@SneakyThrows
	public Dictionary get(ResultSet rs, int position,
			SharedSessionContractImplementor session, Object owner)
			throws SQLException {
		String key = rs.getString(position);
		return fromStringValue(key);
	}

	@Override
	public void set(PreparedStatement st, Dictionary value, int index,
			SharedSessionContractImplementor session) throws SQLException {
		if (value == null) {
			st.setNull(index, getSqlType());
		} else {
			st.setString(index, value.key());
		}
	}

	@Override
	public Dictionary fromStringValue(CharSequence key) throws HibernateException {
		if (key == null) {
			return null;
		}
		var iDictClass = elementType;
		if (iDictClass == null) {
			throw new IllegalStateException("class MUST extend IDict, but was null");
		}
		return Dictionary.of(elementType, key.toString());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setParameterValues(Properties properties) {
		ParameterType parameterType = (ParameterType) properties.get(PARAMETER_TYPE);
		if (parameterType != null) {
			Type returnedJavaType = parameterType.getReturnedJavaType();
			if (returnedJavaType instanceof Class<?> cls) {
				elementType = (Class<? extends Dictionary>) cls;
			} else if (returnedJavaType instanceof java.lang.reflect.ParameterizedType pt) {
				elementType = (Class<? extends Dictionary>) pt.getRawType();
			} else {
				elementType = (Class<? extends Dictionary>) parameterType.getReturnedClass();
			}
		} else {
			String className = properties.getProperty(RETURNED_CLASS);
			if (className != null) {
				try {
					elementType = (Class<? extends Dictionary>)
							Class.forName(className);
				} catch (ClassNotFoundException e) {
					throw new HibernateException(
							"Cannot load elementType class: " + className, e);
				}
			} else {
				throw new HibernateException(
						"Cannot determine elementType: neither PARAMETER_TYPE nor RETURNED_CLASS is set"
				);
			}
		}
	}

}
