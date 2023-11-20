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

import jakarta.persistence.Convert;
import org.cxbox.model.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.cxbox.model.core.hbn.ExtSequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

@Entity
@Table(name = "views") // views, а не view, т.к. это служебное слово oracle
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ExtSequenceGenerator(
		parameters = {
				@Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "META_SEQ"),
				@Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
				@Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "100"),
				@Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "pooled-lo") //StandardOptimizerDescriptor.POOLED_LO
		}
)
public class View extends BaseEntity {

	@Column(unique = true)
	private String name;

	private String template;

	private String title;

	private String url;

	@Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
	private Boolean customizable;

	@Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
	private Boolean editable;

	@Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
	private Boolean ignoreHistory;

	private String options;

	public View() {
		super();
	}

}
