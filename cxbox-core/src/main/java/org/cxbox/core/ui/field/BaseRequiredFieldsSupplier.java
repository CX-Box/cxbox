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

package org.cxbox.core.ui.field;

import org.cxbox.api.data.dto.DataResponseDTO_;
import org.cxbox.core.ui.model.BcField;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;


@Service
public class BaseRequiredFieldsSupplier implements IRequiredFieldsSupplier {

	@Override
	public Collection<BcField> getRequiredFields(String bc) {
		final Set<BcField> fields = new HashSet<>();
		fields.add(new BcField(bc, DataResponseDTO_.id.getName()));
		fields.add(new BcField(bc, DataResponseDTO_.vstamp.getName()));
		return fields;
	}

}
