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

package org.cxbox.model.core.entity;

import static org.hibernate.id.OptimizableGenerator.INCREMENT_PARAM;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cxbox.model.core.hbn.PropagateAnnotations;
import org.cxbox.model.core.listeners.jpa.DelegatingBaseEntityListener;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;

@Audited
@Setter
@Getter
@MappedSuperclass
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@EntityListeners(DelegatingBaseEntityListener.class)
@DiscriminatorOptions(insert = false)
@PropagateAnnotations({DiscriminatorOptions.class})
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class BaseEntity extends AbstractEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "extSequenceGenerator")
	@GenericGenerator(
			name = "extSequenceGenerator",
			type = org.cxbox.model.core.hbn.ExtSequenceStyleGenerator.class,
			parameters = {
					@Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "app_seq"),
					@Parameter(name = INCREMENT_PARAM, value = "1"),
			}
	)
	@JdbcTypeCode(SqlTypes.NUMERIC)
	@Column()
	protected Long id;

	@Version
	@NotAudited
	@Column(name = "vstamp")
	private long vstamp;

	@CreatedDate
	@Column(name = "created_date", nullable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "updated_date", nullable = false)
	private LocalDateTime updatedDate;

	@JdbcTypeCode(SqlTypes.NUMERIC)
	@Column(name = "CREATED_BY_USER_ID", nullable = false)
	private Long createdBy;

	@JdbcTypeCode(SqlTypes.NUMERIC)
	@Column(name = "LAST_UPD_BY_USER_ID", nullable = false)
	private Long lastUpdBy;

	@Transient
	private long loadVstamp = -1;

	@Override
	public String toString() {
		return String.format("%s:%d", getClass().getSimpleName(), getId());
	}

}
