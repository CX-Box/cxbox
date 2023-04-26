/*-
 * #%L
 * IO Cxbox - Workflow Model
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.model.workflow.entity;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.core.entity.User;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Ожидание выполнения перехода
 */
@Getter
@Setter
@Entity
@Table(name = "PENDING_TRANSITION")
public class PendingTransition extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "TRANSITION_ID")
	private WorkflowTransition transition;

	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private User user;

	@Column(name = "USER_ROLE_CD")
	private LOV userRole;

}
