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

package org.cxbox.meta.metahotreload.mapper;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.meta.data.BcSourceBaseDTO;
import org.cxbox.meta.data.BusinessComponentDTO;
import org.cxbox.meta.data.BusinessObjectDTO;
import org.cxbox.meta.data.FilterGroupDTO;
import org.cxbox.meta.data.ScreenNavigation;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.data.ScreenNavigation.MenuItem;
import org.cxbox.meta.data.ScreenNavigation.SingleView;
import org.cxbox.meta.data.ScreenNavigation.ViewGroup;
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.entity.BcProperties;
import org.cxbox.meta.entity.FilterGroup;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto.ScreenNavigationSourceDto.MenuItemSourceDto;
import org.cxbox.meta.metahotreload.util.JsonUtils;
import org.cxbox.meta.ui.model.json.WidgetOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScreenMapper {

	private final AtomicLong screenSeq = new AtomicLong(0L);

	private final AtomicInteger menuItemSeq = new AtomicInteger();

	private final BcRegistry bcRegistry;

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper objectMapper;

	private final MetaConfigurationProperties metaConfigurationProperties;

	public ScreenDTO map(ScreenSourceDto dto, Map<String, ViewDTO> viewNameToView, Map<String, BcProperties> bcProps, Map<String, List<FilterGroup>> filterGroupsAll) {
		List<String> currentScreenViewNames = new ArrayList<>();
		ScreenDTO screenDTO = new ScreenDTO()
				.setName(dto.getName())
				.setTitle(dto.getTitle())
				.setPrimary(dto.getPrimaryViewName())
				.setNavigation(new ScreenNavigation()
						.setMenu(dto.getNavigation().getMenu()
								.stream()
								.map(e -> mapToAndPopulateViewNames(e, currentScreenViewNames))
								.toList()))
				.setViews(intersection(viewNameToView, currentScreenViewNames).toList())
				.setBo(getBusinessObject(intersection(viewNameToView, currentScreenViewNames).collect(Collectors.toMap(ViewDTO::getName, e -> e)), bcProps, filterGroupsAll))
				.setPrimaries(JsonUtils.serializeOrElseNull(objectMapper, dto.getPrimaryViews()));
		screenDTO.setId(String.valueOf(screenSeq.getAndIncrement()));
		return screenDTO;
	}

	private static Stream<ViewDTO> intersection(Map<String, ViewDTO> viewNameToView, List<String> screenViewsNames) {
		return screenViewsNames
				.stream()
				.map(e -> viewNameToView.getOrDefault(e, null))
				.filter(Objects::nonNull);
	}

	private BusinessObjectDTO getBusinessObject(Map<String, ViewDTO> viewDTOs, Map<String, BcProperties> bcProps, Map<String, List<FilterGroup>> filterGroupsAll) {
		BusinessObjectDTO businessObjectDTO = new BusinessObjectDTO(
				viewDTOs.values().stream().map(ViewDTO::getWidgets)
						.flatMap(Collection::stream)
						.filter(widgetDTO -> Objects.nonNull(widgetDTO.getBcName()))
						.map(this::getWidgetBc)
						.flatMap(Collection::stream)
						.peek(this::setBcName)
						.distinct()
						.sorted(Comparator.comparing(BusinessComponentDTO::getUrl))
						.collect(Collectors.toList())
		);
		setBcParameters(businessObjectDTO, bcProps);
		setFilterGroups(businessObjectDTO, filterGroupsAll);
		return businessObjectDTO;
	}

	public Map<String, List<FilterGroup>> getFilterGroups(BusinessObjectDTO boDto, Map<String, List<FilterGroup>> filterGroupsAll) {
		HashMap<String, List<FilterGroup>> result = new HashMap<>(boDto.getBc().size());
		boDto.getBc().forEach(bc -> result.put(bc.getName(), filterGroupsAll.get(bc.getName())));
		return result;
	}


	private void setFilterGroups(final BusinessObjectDTO boDto, Map<String, List<FilterGroup>> filterGroupsAll) {
		Map<String, List<FilterGroup>> filterGroupMap = getFilterGroups(boDto, filterGroupsAll);
		boDto.getBc().forEach(dto -> {
			List<FilterGroup> filterGroups = filterGroupMap.get(dto.getName());
			if (filterGroups != null && !filterGroups.isEmpty()) {
				List<FilterGroupDTO> result = new ArrayList<>();
				filterGroups.forEach(fg -> {
					FilterGroupDTO filterGroupDTO = toFilterGroupDTO(fg);
					result.add(filterGroupDTO);
				});
				dto.setFilterGroups(result);
			}
		});
	}

	public FilterGroupDTO toFilterGroupDTO(FilterGroup entity) {
		var dto = new FilterGroupDTO()
				.setName(entity.getName())
				.setFilters(entity.getFilters())
				.setBc(entity.getBc());

		dto.setId(entity.getId().toString());
		return dto;
	}

	private void setBcParameters(final BusinessObjectDTO boDto, Map<String, BcProperties> bcProps) {
		Map<String, BcProperties> defaultBcPropertiesMap = getStringDefaultBcPropertiesMap(boDto, bcProps);
		boDto.getBc().forEach(dto -> {
			if (metaConfigurationProperties != null) {
				Optional.ofNullable(metaConfigurationProperties.getBcDefaultPageLimit()).ifPresent(dto::setLimit);
			}
			BcProperties bcProperties = defaultBcPropertiesMap.get(dto.getName());
			if (bcProperties != null) {
				Optional.ofNullable(bcProperties.getLimit()).ifPresent(dto::setLimit);
				Optional.ofNullable(bcProperties.getReportPeriod()).ifPresent(dto::setReportPeriod);
				Optional.ofNullable(bcProperties.getSort()).ifPresent(dto::setDefaultSort);
				Optional.ofNullable(bcProperties.getFilter()).ifPresent(dto::setDefaultFilter);
				Optional.ofNullable(bcProperties.getDimFilterSpec()).ifPresent(dto::setDimFilterSpec);
			}
			BcDescription bcDescription = bcRegistry.getBcDescription(dto.getName());
			if (bcDescription != null) {
				Optional.ofNullable(bcDescription.getParentName()).ifPresent(dto::setParentName);
				Optional.ofNullable(bcDescription.isRefresh()).ifPresent(dto::setRefresh);
				Optional.ofNullable(bcDescription.getBindsString()).ifPresent(dto::setBinds);
				Optional.ofNullable(bcDescription.getPageLimit()).ifPresent(dto::setLimit);
			}
		});
	}

	public Map<String, BcProperties> getStringDefaultBcPropertiesMap(BusinessObjectDTO boDto, Map<String, BcProperties> bcProps) {
		Map<String, BcProperties> result = new HashMap<>(boDto.getBc().size());
		boDto.getBc().forEach(bc -> result.put(bc.getName(), bcProps.get(bc.getName())));
		return result;
	}

	private void setBcName(BusinessComponentDTO dto) {
		BcDescription description = bcRegistry.getBcDescription(dto.getName());
		if (description != null) {
			//TODO>>used only for sql bc. Delete after refactoring
			Optional.ofNullable(description.getName()).ifPresent(dto::setName);
		}
	}


	/**
	 * Gets a DB from a widget, taking into account whether widgets with a hierarchy have dependent BCS without their own widget
	 */
	@SneakyThrows
	private List<BusinessComponentDTO> getWidgetBc(WidgetDTO widgetDTO) {
		List<BusinessComponentDTO> result = new ArrayList<>();
		result.add(new BusinessComponentDTO(widgetDTO));
		Optional.ofNullable(widgetDTO.getOptions())
				.map(org.cxbox.core.util.JsonUtils::readTree)
				.filter(JsonNode::isObject)
				.map(options -> org.cxbox.core.util.JsonUtils.readValue(WidgetOptions.class, options))
				.map(WidgetOptions::getHierarchy)
				.ifPresent(list -> list.forEach(item -> {
							String bcName = item.getBcName();
							String url = bcRegistry.getUrlFromBc(bcName);
							result.add(new BusinessComponentDTO(new BcSourceBaseDTO(bcName, url)));
						}
				));
		return result;
	}

	private MenuItem mapToAndPopulateViewNames(MenuItemSourceDto item, List<String> screenViews) {
		MenuItem menuItem;
		if (item.getViewName() != null) {
			SingleView singleView = new SingleView();
			singleView.setViewName(item.getViewName());
			singleView.setHidden(ofNullable(item.getHidden()).orElse(false));
			singleView.setSeq(menuItemSeq.incrementAndGet());
			singleView.setId(String.valueOf(singleView.getSeq()));
			screenViews.add(singleView.getViewName());
			menuItem = singleView;
		} else {
			ViewGroup viewGroup = new ViewGroup();
			viewGroup.setHidden(ofNullable(item.getHidden()).orElse(false));
			viewGroup.setSeq(menuItemSeq.incrementAndGet());
			viewGroup.setId(String.valueOf(viewGroup.getSeq()));
			viewGroup.setDefaultView(item.getDefaultView());
			viewGroup.setTitle(item.getTitle());
			viewGroup.setChild(item.getChild().stream().map(e -> mapToAndPopulateViewNames(e, screenViews)).toList());
			menuItem = viewGroup;
		}
		return menuItem;
	}
}
