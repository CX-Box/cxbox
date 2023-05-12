
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

package org.cxbox.sqlbc.crudma;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.sqlbc.dao.SqlBcQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.cglib.beans.BeanMap;


public class SqlComponentObject extends DataResponseDTO {

	@JsonIgnore
	private final BeanMap fields;

	public SqlComponentObject() {
		fields = BeanMap.create(this);
	}

	public static boolean isIdField(String fieldName) {
		return SqlBcQuery.FIELD_ID.equals(fieldName);
	}

	public BeanMap getFields() {
		return fields;
	}

	public Object get(String fieldName) {
		if (fields.containsKey(fieldName)) {
			return fields.get(fieldName);
		}
		return null;
	}

	public void set(String fieldName, Object value) {
		if (isIdField(fieldName)) {
			setId(String.valueOf(value));
		} else {
			fields.put(fieldName, value);
		}
	}

}
