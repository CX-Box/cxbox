
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

package org.cxbox.model.workflow.entity;

import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.core.hbn.ExtSequenceGenerator;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.OptimizerFactory;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * Настройки атрибутов активности для шага модели переходов
 */
@Getter
@Setter
@Entity
@Table(name = "WF_STEP_FIELD")
@ExtSequenceGenerator(
		parameters = {
				@Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "META_SEQ"),
				@Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
				@Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "100"),
				@Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = OptimizerFactory.POOL_LO)
		}
)
public class WorkflowStepField extends BaseEntity {

	/**
	 * Шаг
	 */
	@ManyToOne
	@JoinColumn(name = "WF_STEP_ID", nullable = false)
	private WorkflowStep step;

	@ManyToOne
	@JoinColumn(name = "TASK_FIELD_ID")
	private TaskField taskField;

}
