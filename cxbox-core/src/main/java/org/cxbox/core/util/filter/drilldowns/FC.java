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


/**
 * This class needed to configure filter parameters for generation url filter string
 */
@Getter(value = AccessLevel.PACKAGE)
public class FC {

	private final List<FCR> FCRs = new ArrayList<>();

	public <D extends DataResponseDTO> FC add(BcIdentifier bc, Class<D> dtoClass,
			Consumer<CxboxFBDefault<D>> consumer) {
		CxboxFBDefault<D> cxboxDrillDownFilterBuilderDefault = new CxboxFBDefault<>() {
		};
		FCR fcr = new FCR(bc, cxboxDrillDownFilterBuilderDefault);
		consumer.accept(cxboxDrillDownFilterBuilderDefault);
		FCRs.add(fcr);
		return this;
	}

	public <D extends DataResponseDTO, F extends FB<D, F>> FC add(
			BcIdentifier bc, Class<D> dtoClass, TypeToken<F> token, Consumer<F> consumer) {
		F fb = token.newInstance();
		FCR fcr = new FCR(bc, fb);
		consumer.accept(fb);
		FCRs.add(fcr);
		return this;
	}


	/**
	 *
	 */
	public record FCR(BcIdentifier bcIdentifier, FB<?, ?> fb) {

	}

}

