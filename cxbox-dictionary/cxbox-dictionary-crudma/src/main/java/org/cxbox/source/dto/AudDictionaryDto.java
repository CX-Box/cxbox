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

package org.cxbox.source.dto;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.model.dictionary.entity.AudDictionary;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AudDictionaryDto extends DataResponseDTO {

	private String eventType;

	private LocalDateTime eventDate;

	private String dictType;

	private String key;

	public AudDictionaryDto(AudDictionary entity) {
		this.id = entity.getId().toString();
		this.eventType = entity.getEventType();
		this.eventDate = entity.getEventDate();
		this.dictType = entity.getDictType();
		this.key = entity.getKey();
	}

}
