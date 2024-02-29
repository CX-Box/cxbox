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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import org.cxbox.core.util.session.SessionService;
import org.cxbox.meta.UIServiceImpl.UserCache;
import org.cxbox.meta.entity.Widget;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.ui.field.IRequiredFieldsSupplier;
import org.cxbox.meta.ui.model.BcField;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Getter
public class BcUtils implements ExtendedDtoFieldLevelSecurityService {

	private final InnerBcTypeAware innerBcTypeAware;

	private final MetaRepository metaRepository;

	private final WidgetUtils widgetUtils;

	private final BcRegistry bcRegistry;

	private final DTOSecurityUtils dtoSecurityUtils;

	private final BcHierarchyAware bcHierarchyAware;

	private final UserCache userCache;

	private final SessionService sessionService;

	private final Optional<List<IRequiredFieldsSupplier>> requiredFieldsSuppliers;

	private final LoadingCache<String, Set<BcField>> bcFields = CacheBuilder
			.newBuilder()
			.build(new BcFieldCacheLoader());

	private final LoadingCache<Long, Set<BcField>> widgetFields = CacheBuilder
			.newBuilder()
			.build(new WidgetFieldCacheLoader());

	private final LoadingCache<String, Map<String, Set<BcField>>> viewFields = CacheBuilder
			.newBuilder()
			.build(new ViewFieldCacheLoader());

	@java.beans.ConstructorProperties({"innerBcTypeAware", "metaRepository", "widgetUtils", "bcRegistry",
			"dtoSecurityUtils", "bcHierarchyAware", "userCache", "sessionService", "requiredFieldsSuppliers"})
	public BcUtils(InnerBcTypeAware innerBcTypeAware, MetaRepository metaRepository, WidgetUtils widgetUtils,
			BcRegistry bcRegistry, DTOSecurityUtils dtoSecurityUtils, BcHierarchyAware bcHierarchyAware, UserCache userCache,
			SessionService sessionService, Optional<List<IRequiredFieldsSupplier>> requiredFieldsSuppliers) {
		this.innerBcTypeAware = innerBcTypeAware;
		this.metaRepository = metaRepository;
		this.widgetUtils = widgetUtils;
		this.bcRegistry = bcRegistry;
		this.dtoSecurityUtils = dtoSecurityUtils;
		this.bcHierarchyAware = bcHierarchyAware;
		this.userCache = userCache;
		this.sessionService = sessionService;
		this.requiredFieldsSuppliers = requiredFieldsSuppliers;
	}


	public void invalidateFieldCache() {
		bcFields.invalidateAll();
		widgetFields.invalidateAll();
		viewFields.invalidateAll();
	}

	/**
	 * @deprecated use {@link #invalidateFieldCache()} instead.
	 */
	@Deprecated
	public void invalidateFieldCacheByView(final String viewName) {
		viewFields.invalidate(viewName);
	}

	/**
	 * @deprecated use {@link #invalidateFieldCache()} instead.
	 */
	@Deprecated
	public void invalidateFieldCacheByWidget(final Long widgetId) {
		widgetFields.invalidate(widgetId);
		metaRepository.getWidget(widgetId).forEach(this::invalidateFieldCacheByView);
	}


	/**
	 * @deprecated use {@link #invalidateFieldCache()} instead.
	 */
	@Deprecated
	public void invalidateFieldCacheByBc(final String bc) {
		bcFields.invalidate(bc);
		metaRepository.getBcWidgets(bc).forEach(this::invalidateFieldCacheByWidget);
	}

	/**
	 * Returns a set of dto fields ({@link DtoField}) for the given business component
	 */
	public <D extends DataResponseDTO> Set<DtoField<D, ?>> getDtoFields(final BcIdentifier bcIdentifier) {
		final BcDescription bcDescription = bcRegistry.getBcDescription(bcIdentifier.getName());
		if (bcDescription instanceof InnerBcDescription) {
			try {
				final InnerBcDescription innerBcDescription = (InnerBcDescription) bcDescription;
				final Class dtoClass = innerBcTypeAware.getTypeOfDto(innerBcDescription);
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
			final Set<BcField> fields = this.viewFields.get(viewName).getOrDefault(bc.getName(), Collections.emptySet());
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
		return userCache.getViews(screenName, sessionService.getSessionUser(), sessionService.getSessionUserRole());
	}

	/**
	 * Returns a set of required dto fields ({@link DtoField}) for the given business component on the current screen
	 */
	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = {
			CacheConfig.REQUEST_CACHE}, key = "{#root.methodName, #bc.name}")
	public <D extends DataResponseDTO> Set<DtoField<D, ?>> getDtoFieldsAvailableOnCurrentScreen(final BcIdentifier bc) {
		final Set<String> viewFields = getBcFieldsForCurrentScreen(bc);
		return getDtoFields(bc).stream()
				.filter(field -> viewFields.contains(field.getName()))
				.map(field -> (DtoField<D, ?>) field)
				.collect(Collectors.toSet());
	}

	private final class BcFieldCacheLoader extends CacheLoader<String, Set<BcField>> {

		@Override
		public Set<BcField> load(final String bc) {
			final Set<BcField> fields = new HashSet<>();
			requiredFieldsSuppliers.ifPresent(suppliers -> suppliers
					.forEach(supplier -> fields.addAll(supplier.getRequiredFields(bc))));
			return fields;
		}

	}

	private final class WidgetFieldCacheLoader extends CacheLoader<Long, Set<BcField>> {

		@Override
		@SneakyThrows
		public Set<BcField> load(final Long widgetId) {
			final Widget widget = metaRepository.getWidgetById(widgetId);
			final Set<BcField> fields = new HashSet<>(widgetUtils.extractAllFields(widget));
			final Set<String> bcNames = fields.stream().map(BcField::getBc).filter(Objects::nonNull)
					.collect(Collectors.toSet());
			for (String bcName : bcNames) {
				fields.addAll(bcFields.get(bcName));
			}
			return fields;
		}

	}

	private final class ViewFieldCacheLoader extends CacheLoader<String, Map<String, Set<BcField>>> {

		@Override
		@SneakyThrows
		public Map<String, Set<BcField>> load(final String viewName) {
			final List<Long> widgetIds = metaRepository.getWidgetByViewName(viewName);
			final Set<BcField> fields = new HashSet<>();
			for (final Long widgetId : widgetIds) {
				fields.addAll(widgetFields.get(widgetId));
			}
			return fields.stream().collect(Collectors.groupingBy(BcField::getBc, Collectors.toSet()));
		}

	}


}
