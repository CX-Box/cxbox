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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.cxbox.core.util.filter.drilldowns.FilterConfiguration.FilterConfigurationMain;
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

		List<FilterConfigurationMain> filterConfigurationMains = configurer.getFilterConfigurationMains();
		List<String> filters = new ArrayList<>();
		filterConfigurationMains.forEach(fc -> {
					if (fc.getFb() instanceof final CxboxDrillDownFilterBuilder<?, ?> cxboxDrillDownFilterBuilder) {
						filters.add(cxboxDrillDownFilterBuilder.build(fc.getBcIdentifier()).orElse(null));
					}
				}
		);
		String filter = filters.stream()
				.filter(Objects::nonNull)
				.filter(s -> !s.isBlank())
				.collect(Collectors.joining(","));

		if (!filter.isBlank()) {
			return "?filters={" + filter + "}";
		}
		return null;
	}

}
