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

import static org.hibernate.id.OptimizableGenerator.INCREMENT_PARAM;
import static org.hibernate.id.OptimizableGenerator.INITIAL_PARAM;
import static org.hibernate.id.OptimizableGenerator.OPT_PARAM;

import org.cxbox.model.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import org.cxbox.model.core.hbn.ExtSequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

@Getter
@Setter
@Entity
@Accessors(chain = true)
@ExtSequenceGenerator(
		parameters = {
				@Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "META_SEQ"),
				@Parameter(name = INITIAL_PARAM, value = "1"),
				@Parameter(name = INCREMENT_PARAM, value = "100"),
				@Parameter(name = OPT_PARAM, value = "pooled-lo") //StandardOptimizerDescriptor.POOLED_LO
		}
)
public class Screen extends BaseEntity {

	@Column(unique = true)
	private String name;

	private String title;

	@Column(name = "primary_view_name")
	private String primary;

	// TODo Выпилить после показа CBR-1488
	@Column(name = "primary_views")
	private String primaries;

}
