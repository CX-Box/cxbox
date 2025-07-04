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
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;


@Getter(value = AccessLevel.PACKAGE)
public class FilterConfiguration {


	private final List<FilterConfigurationMain> filterConfigurationMains = new ArrayList<>();

	public FilterConfiguration add(Consumer<FilterConfigurationMain> function) {
		FilterConfigurationMain fcm = new FilterConfigurationMain();
		function.accept(fcm);
		filterConfigurationMains.add(fcm);
		return this;
	}

	public <DTO extends DataResponseDTO> FilterConfiguration add(BcIdentifier bcIdentifier, Class<DTO> dtoClass, Consumer<CxboxDrillDownFilterBuilderDefault<DTO>> consumer) {
		FilterConfigurationMain filterConfigurationMain = new FilterConfigurationMain();
		filterConfigurationMain.bcIdentifier = bcIdentifier;
		CxboxDrillDownFilterBuilderDefault<DTO> cxboxDrillDownFilterBuilderDefault = new CxboxDrillDownFilterBuilderDefault<>() {
		};
		filterConfigurationMain.fb = cxboxDrillDownFilterBuilderDefault;
		consumer.accept(cxboxDrillDownFilterBuilderDefault);
		filterConfigurationMains.add(filterConfigurationMain);
		return this;
	}

	public <DTO extends DataResponseDTO, FB extends FilterBuilder<DTO, FB>> FilterConfiguration add(
			BcIdentifier bcIdentifier, Class<DTO> dtoClass, TypeToken<FB> token, Consumer<FB> consumer) {
		FilterConfigurationMain filterConfigurationMain = new FilterConfigurationMain();
		filterConfigurationMain.bcIdentifier = bcIdentifier;
		FB fb = token.newInstance();
		filterConfigurationMain.fb = fb;
		consumer.accept(fb);
		filterConfigurationMains.add(filterConfigurationMain);
		return this;
	}


	@Getter
	public static class FilterConfigurationMain {

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

	}


	public static class FilterConfigurationFilterBuilderBuilder<DTO extends DataResponseDTO, FB extends FilterBuilder<DTO, FB>> {

		private final FilterConfigurationMain filterConfigurationMain;

		private final FB fb;

		public FilterConfigurationFilterBuilderBuilder(FilterConfigurationMain filterConfigurationMain, FB fb) {
			this.filterConfigurationMain = filterConfigurationMain;
			this.fb = fb;
		}

		public FilterConfigurationMain filters(Consumer<FB> consumer) {
			consumer.accept(this.fb);
			return this.filterConfigurationMain;
		}

	}

}

