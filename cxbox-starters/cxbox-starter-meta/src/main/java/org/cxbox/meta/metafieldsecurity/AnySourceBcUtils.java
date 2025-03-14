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

package org.cxbox.meta.metafieldsecurity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.bc.AnySourceBcTypeAware;
import org.cxbox.core.bc.InnerBcTypeAware;
import org.cxbox.core.controller.BcHierarchyAware;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.impl.AnySourceBcDescription;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.service.DTOSecurityUtils;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.ui.field.IRequiredFieldsSupplier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class AnySourceBcUtils extends BcUtils {

	private final AnySourceBcTypeAware anySourceBcTypeAware;

	public AnySourceBcUtils(InnerBcTypeAware innerBcTypeAware, MetaRepository metaRepository, WidgetUtils widgetUtils,
			BcRegistry bcRegistry, DTOSecurityUtils dtoSecurityUtils, BcHierarchyAware bcHierarchyAware, ResponsibilitiesService responsibilitiesService,
			SessionService sessionService, Optional<List<IRequiredFieldsSupplier>> requiredFieldsSuppliersm, AnySourceBcTypeAware anySourceBcTypeAware, ViewFieldsCache viewFieldsCache) {
		super(innerBcTypeAware, metaRepository, widgetUtils, bcRegistry, dtoSecurityUtils, bcHierarchyAware, responsibilitiesService,
				sessionService,  requiredFieldsSuppliersm, viewFieldsCache);
		this.anySourceBcTypeAware = anySourceBcTypeAware;
	}

	/**
	 * Returns a set of dto fields ({@link DtoField}) for the given business component
	 */
	@Override
	public <D extends DataResponseDTO> Set<DtoField<D, ?>> getDtoFields(final BcIdentifier bcIdentifier) {
		final BcDescription bcDescription = getBcRegistry().getBcDescription(bcIdentifier.getName());
		if (bcDescription instanceof InnerBcDescription innerBcDescription) {
			try {
				final Class<D> dtoClass = (Class<D>) getInnerBcTypeAware().getTypeOfDto(innerBcDescription);
				return getDtoSecurityUtils().getDtoFields(dtoClass);
			} catch (RuntimeException e) {
				return Collections.emptySet();
			}
		}
		if (bcDescription instanceof AnySourceBcDescription anySourceBcDescription) {
			try {
				final Class<D> dtoClass = (Class<D>) anySourceBcTypeAware.getTypeOfDto(anySourceBcDescription);
				return getDtoSecurityUtils().getDtoFields(dtoClass);
			} catch (RuntimeException e) {
				return Collections.emptySet();
			}
		}
		return Collections.emptySet();
	}

}
