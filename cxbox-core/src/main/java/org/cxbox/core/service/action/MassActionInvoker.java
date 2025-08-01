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

package org.cxbox.core.service.action;

import java.util.Set;
import lombok.NonNull;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.MassDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dto.rowmeta.MassActionResultDTO;

@FunctionalInterface
public interface MassActionInvoker<T extends DataResponseDTO> {

	MassActionResultDTO<T> massInvoke(@NonNull BusinessComponent bc, @NonNull T data, @NonNull Set<String> ids);

	default ActionInvoker<T> toInvoker() {
		return (bc, data) -> massInvoke(bc, data, data.getMassIds_().stream()
				.map(MassDTO::getId)
				.map(String::valueOf)
				.collect(java.util.stream.Collectors.toSet())
		);
	}

}
