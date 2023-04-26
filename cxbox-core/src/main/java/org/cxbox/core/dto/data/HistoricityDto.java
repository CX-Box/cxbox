/*-
 * #%L
 * IO Cxbox - Core
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

package org.cxbox.core.dto.data;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.model.core.entity.HistoricityEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class HistoricityDto extends DataResponseDTO {

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	public HistoricityDto(final HistoricityEntity entity) {
		this.id = entity.getId().toString();
		this.startDate = entity.getStartDate();
		this.endDate = entity.getEndDate();
	}

}
