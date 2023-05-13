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
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.OptimizerFactory;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

@Getter
@Setter
@Entity
@Table(name = "WF_TRANSITION_GROUP")
@ExtSequenceGenerator(
		parameters = {
				@Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "META_SEQ"),
				@Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
				@Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "100"),
				@Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = OptimizerFactory.POOL_LO)
		}
)
public class WorkflowTransitionGroup extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "WF_STEP_ID", nullable = false)
	private WorkflowStep workflowStep;

	@Column(name = "MAX_SHOW_BUTTONS_IN_GROUP")
	private int maxShowButtonsInGroup;

	@Column(name = "MORE_NAME")
	private String nameButtonYet;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "SEQ")
	private Long seq;

	@OneToMany(mappedBy = "workflowTransitionGroup", cascade = {CascadeType.DETACH})
	private Set<WorkflowTransition> workflowTransitions;

	@PreRemove
	private void preRemove() {
		workflowTransitions.forEach(wt -> wt.setWorkflowTransitionGroup(null));
	}

}
