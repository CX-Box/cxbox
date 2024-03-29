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

package org.cxbox.core.crudma.bc;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.cxbox.api.data.BcIdentifier;


public interface BcOverrider {

	List<BcOverride> getBcOverrides();

	@Getter
	class BcOverride {

		private final List<BcIdentifier> bcIdentifiers;

		private final Class<?> serviceClass;

		public BcOverride(final Class<?> serviceClass, final BcIdentifier... bcIdentifiers) {
			this.serviceClass = serviceClass;
			this.bcIdentifiers = Arrays.asList(bcIdentifiers);
		}

	}

}
