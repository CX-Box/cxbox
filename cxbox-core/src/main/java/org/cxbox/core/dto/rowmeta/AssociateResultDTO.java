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

package org.cxbox.core.dto.rowmeta;

import org.cxbox.api.data.dto.DataResponseDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class AssociateResultDTO {

	private final List<DataResponseDTO> records;

	private final List<PostAction> postActions = new ArrayList<>();

	public AssociateResultDTO(List<DataResponseDTO> records) {
		this.records = records;
	}

	public AssociateResultDTO setAction(PostAction postAction) {
		this.postActions.add(postAction);
		return this;
	}

}
