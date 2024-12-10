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

package org.cxbox.meta.metafieldsecurity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cxbox.api.ExtendedDtoFieldLevelSecurityService;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.bc.InnerBcTypeAware;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.core.controller.BcHierarchyAware;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.service.DTOSecurityUtils;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.ui.field.IRequiredFieldsSupplier;
import org.cxbox.meta.ui.model.BcField;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Getter
@RequiredArgsConstructor
public class BcUtils implements ExtendedDtoFieldLevelSecurityService {

	private final InnerBcTypeAware innerBcTypeAware;

	private final MetaRepository metaRepository;

	private final WidgetUtils widgetUtils;

	private final BcRegistry bcRegistry;

	private final DTOSecurityUtils dtoSecurityUtils;

	private final BcHierarchyAware bcHierarchyAware;

	private final ResponsibilitiesService responsibilitiesService;

	private final SessionService sessionService;

	private final Optional<List<IRequiredFieldsSupplier>> requiredFieldsSuppliers;

	private final ViewFieldsCache viewFieldsCache;


	/**
	 * Returns a set of dto fields ({@link DtoField}) for the given business component
	 */
	public <D extends DataResponseDTO> Set<DtoField<D, ?>> getDtoFields(final BcIdentifier bcIdentifier) {
		final BcDescription bcDescription = bcRegistry.getBcDescription(bcIdentifier.getName());
		if (bcDescription instanceof InnerBcDescription innerBcDescription) {
			try {
				final Class<D> dtoClass = (Class<D>) innerBcTypeAware.getTypeOfDto(innerBcDescription);
				return dtoSecurityUtils.getDtoFields(dtoClass);
			} catch (RuntimeException e) {
				return Collections.emptySet();
			}
		}
		return Collections.emptySet();
	}

	/**
	 * Returns a set of required fields for the given business component on the current screen
	 */
	@SneakyThrows
	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = {
			CacheConfig.REQUEST_CACHE}, key = "{#root.methodName, #bc.name}")
	public Set<String> getBcFieldsForCurrentScreen(final BcIdentifier bc) {
		final Set<String> viewFields = new HashSet<>();
		for (final String viewName : getCurrentScreenViews()) {
			final Set<BcField> fields = this.viewFieldsCache.getDtoFieldsAvailableOnCurrentView(viewName)
					.getOrDefault(bc.getName(), Collections.emptySet());
			for (final BcField field : fields) {
				viewFields.add(field.getName());
			}
		}
		return viewFields;
	}

	public Collection<String> getCurrentScreenViews() {
		return getViews(bcHierarchyAware.getScreenName());
	}

	public List<String> getViews(final String screenName) {
		return responsibilitiesService.getAvailableScreenViews(
				screenName,
				sessionService.getSessionUser(),
				sessionService.getSessionUserRoles()
		);
	}

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = {
			CacheConfig.REQUEST_CACHE}, key = "{#root.methodName, #bc.name}")
	public <D extends DataResponseDTO> Set<DtoField<D, ?>> getDtoFieldsAvailableOnCurrentScreen(final BcIdentifier bc) {
		final Set<String> viewFields = getBcFieldsForCurrentScreen(bc);
		return getDtoFields(bc).stream()
				.filter(field -> viewFields.contains(field.getName()))
				.map(field -> (DtoField<D, ?>) field)
				.collect(Collectors.toSet());
	}

	@Component
	@RequiredArgsConstructor
	public static class ViewFieldsCache {

		final MetaRepository metaRepository;

		final WidgetUtils widgetUtils;

		final Optional<List<IRequiredFieldsSupplier>> requiredFieldsSuppliers;

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.targetClass, #root.methodName, #viewName}"
		)
		public Map<String, Set<BcField>> getDtoFieldsAvailableOnCurrentView(final String viewName) {
			final Set<BcField> fields = new HashSet<>();
			metaRepository.getAllScreens().forEach((name, screen) -> ((ScreenDTO) screen.getMeta()).getViews().forEach(view -> {
				if (Objects.equals(view.getName(), viewName)) {
					view.getWidgets().forEach(widget -> {
						var widgetFields = new HashSet<>(widgetUtils.extractAllFields(widget));
						widgetFields.stream().map(BcField::getBc).filter(Objects::nonNull)
								.forEach(bc -> fields.addAll(getRequiredBcFields(bc)));
						fields.addAll(widgetFields);
					});
				}
			}));

			return fields.stream().collect(Collectors.groupingBy(BcField::getBc, Collectors.toSet()));
		}

		@NonNull
		public Set<BcField> getRequiredBcFields(final @NonNull String bc) {
			final Set<BcField> fields = new HashSet<>();
			requiredFieldsSuppliers.ifPresent(suppliers -> suppliers
					.forEach(supplier -> fields.addAll(supplier.getRequiredFields(bc))));
			return fields;
		}

	}

}
