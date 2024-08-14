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

package org.cxbox.core.service;

import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.dto.DTOUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DTOSecurityUtils {

	/**
	 * Returns a set of dto fields ({@link DtoField}) for the given dto class
	 */
	@SneakyThrows
	public <D extends DataResponseDTO> Set<DtoField<D, ?>> getDtoFields(final Class<D> dtoClass) {
		return DTOUtils.getAllFields(dtoClass);
	}

}
