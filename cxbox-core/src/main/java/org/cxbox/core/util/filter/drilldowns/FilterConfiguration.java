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


import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;


@Getter(value = AccessLevel.PACKAGE)
public class FilterConfiguration {

	private BcIdentifier bcIdentifier;

	private FilterBuilder<?, ?> fb;

	public <DTO extends DataResponseDTO, FB extends FilterBuilder<DTO, FB>> FilterConfigurationFilterBuilderBuilder<DTO, FB> customBuilder(
			BcIdentifier bc, TypeToken<FB> token) {
		FB fb = token.newInstance();
		this.fb = fb;
		this.bcIdentifier = bc;
		return new FilterConfigurationFilterBuilderBuilder<DTO, FB>(this, fb);
	}

	public <DTO extends DataResponseDTO, FB extends CxboxDrillDownFilterBuilder<DTO, FB>> FilterConfigurationFilterBuilderBuilder<DTO, FB> defaultBuilder(
			BcIdentifier bc, Class<DTO> dtoClass) {
		FB fb = (FB) new CxboxDrillDownFilterBuilder<DTO, FB>() {
		};
		this.fb = fb;
		this.bcIdentifier = bc;
		return new FilterConfigurationFilterBuilderBuilder<>(this, fb);
	}


	public static class FilterConfigurationFilterBuilderBuilder<DTO extends DataResponseDTO, FB extends FilterBuilder<DTO, FB>> {

		private final FilterConfiguration filterConfiguration;

		private final FB fb;

		public FilterConfigurationFilterBuilderBuilder(FilterConfiguration filterConfiguration, FB fb) {
			this.filterConfiguration = filterConfiguration;
			this.fb = fb;
		}

		public FilterConfiguration filters(Consumer<FB> consumer) {
			consumer.accept(this.fb);
			return this.filterConfiguration;
		}

	}

}

