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

package org.cxbox.model.ui.entity;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.cxbox.model.core.entity.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cxbox.model.core.entity.User;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Table(name = "BC_FILTER_GROUPS")
@Accessors(chain = true)
public class FilterGroup extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	private String name;

	private String filters;

	private String bc;

}