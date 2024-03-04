/*
 * © OOO "SI IKS LAB", 2022-2024
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

package org.cxbox.core.bc;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.AnySourceBcDescription;
import org.cxbox.core.service.AnySourceResponseFactory;
import org.springframework.stereotype.Service;

@Service
public class AnySourceBcTypeAware {

	private final Map<String, AnySourceBcTypes> types;

	public AnySourceBcTypeAware(final BcRegistry bcRegistry, final AnySourceResponseFactory respFactory) {
		types = bcRegistry.select(AnySourceBcDescription.class).collect(Collectors.toMap(
				BcDescription::getName,
				bcDescription -> new AnySourceBcTypes(respFactory.getResponseServiceParameters(bcDescription))
		));
	}

	public Class<? extends DataResponseDTO> getTypeOfDto(final AnySourceBcDescription bcDescription) {
		return types.get(bcDescription.getName()).getDto();
	}

	@Getter
	private static class AnySourceBcTypes {

		private final Class<?> entity;

		private final Class<? extends DataResponseDTO> dto;

		AnySourceBcTypes(final Type[] responseServiceTypes) {
			this.entity = (Class<?>) responseServiceTypes[1];
			this.dto = (Class<? extends DataResponseDTO>) responseServiceTypes[0];
		}

	}

}
