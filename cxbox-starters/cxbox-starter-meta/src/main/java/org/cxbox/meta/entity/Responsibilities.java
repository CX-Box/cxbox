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

import static org.hibernate.id.OptimizableGenerator.INCREMENT_PARAM;
import static org.hibernate.id.OptimizableGenerator.INITIAL_PARAM;
import static org.hibernate.id.OptimizableGenerator.OPT_PARAM;

import java.sql.Types;
import org.cxbox.api.data.dictionary.LOV;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.core.hbn.ExtSequenceGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "RESPONSIBILITIES")
@ExtSequenceGenerator(
		parameters = {
				@Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "META_SEQ"),
				@Parameter(name = INITIAL_PARAM, value = "1"),
				@Parameter(name = INCREMENT_PARAM, value = "100"),
				@Parameter(name = OPT_PARAM, value = "pooled-lo") //StandardOptimizerDescriptor.POOLED_LO
		}
)
public class Responsibilities extends BaseEntity {

	@Column(name = "INTERNAL_ROLE_CD")
	private LOV internalRoleCD;

	@JdbcTypeCode(SqlTypes.NUMERIC)
	@Column(name = "DEPT_ID")
	private Long departmentId;

	@Column(name = "RESPONSIBILITIES")
	private String view;

	@Column(name = "RESP_TYPE")
	@Enumerated(EnumType.STRING)
	private ResponsibilityType responsibilityType;

	@Column(name = "READ_ONLY")
	private boolean readOnly;

	@Lob
	@JdbcTypeCode(Types.CLOB)
	private String screens;

	public enum ResponsibilityType {
		VIEW,
		SCREEN
	}

}
