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

package org.cxbox.meta.entity;

import jakarta.persistence.Convert;
import java.sql.Types;
import org.cxbox.model.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Getter
@Setter
@Entity
@Table(name = "bc")
@Accessors(chain = true)
public class Bc extends BaseEntity {

	@Column(name = "name")
	private String name;

	@Column(name = "parent_name")
	private String parentName;

	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)
	@Column(name = "query")
	private String query;

	@Column(name = "binds")
	private String binds;

	@Column(name = "default_order")
	private String defaultOrder;

	@Column(name = "report_date_field")
	private String reportDateField;

	@JdbcTypeCode(SqlTypes.NUMERIC)
	@Column(name = "page_limit")
	private Long pageLimit;

	@Column(name = "editable")
	@Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
	private Boolean editable;

	@Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
	@Column(name = "refresh")
	private Boolean refresh;

}
