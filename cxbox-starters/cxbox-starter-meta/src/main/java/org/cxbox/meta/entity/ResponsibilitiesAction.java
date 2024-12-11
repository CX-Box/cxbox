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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.core.hbn.ExtSequenceGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "responsibilities_action")
@ExtSequenceGenerator(
		parameters = {
				@Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "META_SEQ"),
				@Parameter(name = INITIAL_PARAM, value = "1"),
				@Parameter(name = INCREMENT_PARAM, value = "100"),
				@Parameter(name = OPT_PARAM, value = "pooled-lo")
		}
)
@EqualsAndHashCode(callSuper = true)
public class ResponsibilitiesAction extends BaseEntity {

	public static final String ANY_INTERNAL_ROLE_CD = "*";

	public static final String ANY_VIEW = "*";

	/**
	 * role or *
	 */
	@Column(name = "INTERNAL_ROLE_CD")
	private String internalRoleCD = ANY_INTERNAL_ROLE_CD;


	@Column(name = "ACTION")
	private String action;

	/**
	 * .view.json -> name or *
	 */
	@Column(name = "VIEW")
	private String view = ANY_VIEW;

	@Column(name = "WIDGET")
	private String widget;

	public boolean isAvailable(@NonNull Set<String> roles, @NonNull String view, @NonNull String widget) {
		return (ANY_INTERNAL_ROLE_CD.equals(this.getInternalRoleCD()) || roles.contains(this.getInternalRoleCD())) &&
				(ANY_VIEW.equals(this.getView()) || view.contains(this.getView())) &&
				Objects.equals(widget, this.getWidget());
	}

}
