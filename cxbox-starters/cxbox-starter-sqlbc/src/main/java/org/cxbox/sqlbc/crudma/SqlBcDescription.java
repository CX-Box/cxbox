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

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.util.tz.TimeZoneUtil;
import org.cxbox.core.controller.param.SearchOperation;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.meta.metahotreload.dto.BcSourceDTO;
import org.cxbox.meta.metahotreload.util.JsonUtils;
import org.cxbox.sqlbc.dao.SqlFieldType;
import org.cxbox.sqlbc.exception.BadSqlComponentException;
import org.springframework.beans.factory.annotation.Qualifier;


public final class SqlBcDescription extends BcDescription {

	@Getter
	private final String query;

	@Getter
	private final String defaultOrder;

	@Getter
	private final String reportDateField;

	@Getter
	private final boolean editable;

	private final LazyInitializer<List<Field>> fieldsInitializer;

	@Getter
	private final List<Bind> binds;

	public SqlBcDescription(@Qualifier(CxboxBeanProperties.OBJECT_MAPPER) ObjectMapper objectMapper, BcSourceDTO bc, List<Bind> binds,
			LazyInitializer<List<Field>> fieldsInitializer) {
		super(
				bc.getName(),
				bc.getParentName(),
				SqlCrudmaService.class,
				Boolean.TRUE.equals(ofNullable(bc.getRefresh()).map(val -> val > 0).orElse(false))
		);
		this.query = bc.getQuery();
		this.defaultOrder = bc.getDefaultOrder();
		this.reportDateField = bc.getReportDateField();
		this.pageLimit = bc.getPageLimit();
		this.editable = Boolean.TRUE.equals(ofNullable(bc.getEditable()).map(val -> val > 0).orElse(false));
		this.fieldsInitializer = fieldsInitializer;
		this.binds = binds;
		this.bindsString = JsonUtils.serializeOrElseNull(objectMapper, bc.getBinds());
	}

	public List<Field> getFields() {
		try {
			return fieldsInitializer.get();
		} catch (Exception e) {
			throw new BadSqlComponentException(getName(), e);
		}
	}

	@Getter
	@AllArgsConstructor
	public static final class Field {

		private final String columnName;

		private final SqlFieldType type;

		private final Boolean editable;

		public String getFieldName() {
			return columnName.toLowerCase();
		}

		public boolean isTzAware() {
			return TimeZoneUtil.hasTzAwareSuffix(this.getFieldName()) && this.getType().isChronological();
		}

	}

	@Getter
	@AllArgsConstructor
	public static final class Bind {

		private String bindName;

		private SearchOperation type;

		public boolean isExistInQuery(String query) {
			return query.contains(":" + getBindName());
		}

	}

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class Bindings {

		private String title;

		private String key;

		private String type;

		private List<Map<String, Object>> operations;

		private String popupBcName;

		private Map<String, Object> pickMap;

		private List<String> dictionaryValues;

		private Boolean permanent;


	}

}
