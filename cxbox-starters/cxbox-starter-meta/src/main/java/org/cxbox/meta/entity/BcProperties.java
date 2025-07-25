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

import org.cxbox.model.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Table(name = "BC_PROPERTIES")
@Accessors(chain = true)
public class BcProperties extends BaseEntity {

	String bc;

	String filter;

	@JdbcTypeCode(SqlTypes.NUMERIC)
	@Column(name = "PAGE_LIMIT")
	Long limit;

	@JdbcTypeCode(SqlTypes.NUMERIC)
	@Column(name = "MASS_PAGE_LIMIT")
	Long massLimit;

	String sort;

	Long reportPeriod;

	String dimFilterSpec;

}
