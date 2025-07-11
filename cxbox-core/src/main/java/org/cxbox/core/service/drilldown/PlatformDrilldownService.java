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

package org.cxbox.core.service.drilldown;

import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cxbox.core.service.drilldown.filter.FB;
import org.cxbox.core.service.drilldown.filter.FC;
import org.cxbox.core.service.drilldown.filter.PlatformDrilldownFilterService;

@RequiredArgsConstructor
public class PlatformDrilldownService {

	private final PlatformDrilldownFilterService platformDrilldownFilterService;

	/**
	 * This method generate URL parameters with filters as {@link String}
	 * <p><b>Note!</b> If you use {@link PlatformDrilldownService}  builder must be extended {@link FB} on {@link FC}</p>
	 * <pre>
	 * {@code
	 * // Adding filter parameters to the URL from {@link FC}, seems like:
	 * //	filters = {
	 * //		“bcName”:“field1.equals=data1”,
	 * //		"bcName2":“field1.equals=data1”
	 * //	}
	 * baseUrl += fc.formUrlFilterPart(fc)}
	 * </pre>
	 *
	 * @param fc {@code FC} configurer filter params
	 * @return {@code String} - URL parameters with filters
	 */
	public String formUrlFilterPart(FC fc) {
		String filters = fc.getFCRs().stream()
				.filter(Objects::nonNull)
				.map(fcr -> platformDrilldownFilterService.formUrlPart(fcr.bcIdentifier(), fcr.filterStrings()).orElse(null))
				.filter(Objects::nonNull)
				.filter(s -> !s.isBlank())
				.collect(Collectors.joining(","));
		if (!filters.isBlank()) {
			return "filters={" + filters + "}";
		}
		return null;
	}

}
