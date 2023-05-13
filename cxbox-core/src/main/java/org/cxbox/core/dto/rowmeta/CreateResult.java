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

import org.cxbox.api.data.dto.DataResponseDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateResult<D extends DataResponseDTO> {

	private final D record;

	private final List<PostAction> postActions = new ArrayList<>();

	public CreateResult<D> setAction(final PostAction postAction) {
		this.postActions.add(postAction);
		return this;
	}

	public CreateResult<D> setActions(final List<PostAction> postActions) {
		this.postActions.addAll(postActions);
		return this;
	}

}
