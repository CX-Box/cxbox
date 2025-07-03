/*
 * © OOO "SI IKS LAB", 2022-2025
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

package org.cxbox.core.util.filter.drilldowns;

import org.cxbox.api.data.BcIdentifier;
import org.springframework.stereotype.Service;

@Service
public class CxboxDrilldownFilterFormerService implements DrilldownFilterFormerService {

//
//	@Override
//	public <D extends DataResponseDTO, FB extends CxboxDrillDownFilterBuilder<D,FB>> Supplier<FB> supplierDrilldown() {
//		return () -> new CxboxDrillDownFilterBuilder<D,FB>() {};
//	}
//
//	@Override
//	public <D extends DataResponseDTO> String formDrillDownFilter(BcIdentifier bc, Class<D> dtoClass,
//			UnaryOperator<CxboxDrillDownFilterBuilder<D>> configurer) {
//		return configurer.apply((CxboxDrillDownFilterBuilder<D>) supplierDrilldown().get()).build(bc).orElse(null);
//	}

	@Override
	public String formDrillDownFilter(FilterConfiguration configurer) {
		BcIdentifier bcIdentifier = configurer.getBcIdentifier();
		FilterBuilder<?, ?> fb = configurer.getFb();
		if (fb instanceof final CxboxDrillDownFilterBuilder<?, ?> cxboxDrillDownFilterBuilder) {
			return cxboxDrillDownFilterBuilder.build(bcIdentifier).orElse("");
		}
		throw new RuntimeException("Cannot form drilldowns");
	}

}
