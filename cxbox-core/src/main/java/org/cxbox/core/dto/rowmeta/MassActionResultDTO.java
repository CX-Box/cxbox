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

package org.cxbox.core.dto.rowmeta;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import org.cxbox.api.data.IDataContainer;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.MassDTO;

public class MassActionResultDTO<T extends DataResponseDTO> extends ActionResultDTO<T> implements IDataContainer<T> {

	private final HashSet<MassDTO> massIds_;

	@JsonProperty("massIds_")
	public Set<MassDTO> getMassIds_() {
		return massIds_;
	}

	@Override
	public MassActionResultDTO<T> setAction(PostAction postAction) {
		super.setAction(postAction);
		return this;
	}

	@Override
	public MassActionResultDTO<T> setActions(List<PostAction> postActions) {
		super.setActions(postActions);
		return this;
	}

	public MassActionResultDTO(@NonNull Set<MassDTO> massIds) {
		this.massIds_ = new HashSet<>(massIds);
	}

}
