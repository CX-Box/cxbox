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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

/**
 * Проверка возможности перехода
 */
@Getter
@Setter
@Entity
@Table(name = "WF_TRANSITION_VALID")
public class WorkflowTransitionValidation extends BaseEntity {

	/**
	 * Группа условий
	 */
	@ManyToOne
	@JoinColumn(name = "COND_GROUP_ID", nullable = false)
	private WorkflowTransitionConditionGroup conditionGroup;

	/**
	 * Порядок
	 */
	private Long seq;

	/**
	 * Тип валидации
	 */
	private LOV validCd;

	private String errorMessage;

	/**
	 * DMN валидация
	 */
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String dmn;

	/**
	 * Тип предварительного действия
	 */
	@Column(name = "PRE_INVOKE_TYPE_CD")
	private LOV preInvokeTypeCd;

	/**
	 * Условие предварительного действия
	 */
	@Column(name = "PRE_INVOKE_COND_CD")
	private LOV preInvokeCondCd;

	/**
	 * Сообщение предварительного действия
	 */
	@Column(name = "PRE_INVOKE_MESSAGE")
	private String preInvokeMessage;

}
