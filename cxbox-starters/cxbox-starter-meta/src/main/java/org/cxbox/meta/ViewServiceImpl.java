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

package org.cxbox.meta;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.meta.data.FilterGroupDTO;
import org.cxbox.core.exception.ClientException;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.meta.data.view.BcSourceBaseDTO;
import org.cxbox.meta.data.view.BusinessComponentDTO;
import org.cxbox.meta.data.view.BusinessObjectDTO;
import org.cxbox.meta.data.view.ScreenBuildMeta;
import org.cxbox.meta.data.view.ScreenDTO;
import org.cxbox.meta.data.view.ViewDTO;
import org.cxbox.meta.data.view.WidgetDTO;
import org.cxbox.meta.entity.BcProperties;
import org.cxbox.meta.entity.FilterGroup;
import org.cxbox.meta.entity.Screen;
import org.cxbox.meta.entity.View;
import org.cxbox.meta.entity.ViewWidgets;
import org.cxbox.meta.entity.Widget;
import org.cxbox.meta.metafieldsecurity.BcUtils;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.ui.model.json.WidgetOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ViewServiceImpl implements ViewService {

	private final BcUtils bcUtils;

	private final BcRegistry bcRegistry;

	private final UIServiceImpl uiService;

	private final SessionService sessionService;

	private final MetaRepository metaRepository;

	private ViewDTO buildViewDTO(View view,
			Map<String, List<ViewWidgets>> allViewWidgets,
			Map<String, Boolean> responsibilities) {
		int widgetIdCounter = 0;
		List<ViewWidgets> viewWidgetsList = allViewWidgets.get(view.getName());
		if (viewWidgetsList == null) {
			viewWidgetsList = Collections.emptyList();
		}
		ViewDTO result = toViewDTO(view);
		result.setReadOnly(Optional.ofNullable(responsibilities.get(view.getName())).orElse(false));
		List<WidgetDTO> list = new ArrayList<>();
		for (ViewWidgets widgetWithPosition : viewWidgetsList) {
			WidgetDTO widgetDTO = toWidgetDTO(widgetWithPosition, widgetIdCounter);
			widgetDTO.setUrl(bcRegistry.getUrlFromBc(widgetWithPosition.getWidget().getBc()));
			list.add(widgetDTO);
			widgetIdCounter++;
		}
		result.setWidgets(list);
		return result;
	}

	public WidgetDTO toWidgetDTO(ViewWidgets widgetWithPosition, int widgetIdCounter) {
		WidgetDTO dto = toWidgetDTO(widgetWithPosition.getWidget());
		dto.setPosition(widgetWithPosition.getPositon() != null ? widgetWithPosition.getPositon() : 0);
		dto.setGridWidth(widgetWithPosition.getGridWidth() != null ? widgetWithPosition.getGridWidth() : 1);
		dto.setGridBreak(widgetWithPosition.getGridBreak() != null ? widgetWithPosition.getGridBreak() : 0);
		dto.setLimit(widgetWithPosition.getLimit() != null ? widgetWithPosition.getLimit() : 0);
		dto.setDescriptionTitle(widgetWithPosition.getDescriptionTitle());
		dto.setDescription(widgetWithPosition.getDescription());
		dto.setSnippet(widgetWithPosition.getSnippet());
		dto.setShowExportStamp(widgetWithPosition.getShowExportStamp());
		dto.setWidgetId(widgetIdCounter);
		dto.setHide(widgetWithPosition.getHide());
		return dto;
	}

	public WidgetDTO toWidgetDTO(Widget widget) {
		WidgetDTO dto = new WidgetDTO();
		dto.setId(widget.getId().toString());
		dto.setName(widget.getName());
		dto.setType(widget.getType());
		dto.setBcName(widget.getBc());
		dto.setTitle(widget.getTitle());
		dto.setFields(widget.getFields());
		dto.setOptions(widget.getOptions());
		dto.setAxisFields(widget.getAxisFields());
		dto.setPivotFields(widget.getPivotFields());
		dto.setShowCondition(widget.getShowCondition());
		dto.setChart(widget.getChart());
		dto.setGraph(widget.getGraph());
		return dto;
	}

	public ViewDTO toViewDTO(View view) {
		ViewDTO dto = new ViewDTO();
		dto.setId(view.getId());
		dto.setName(view.getName());
		dto.setTemplate(view.getTemplate());
		dto.setCustomizable(view.getCustomizable());
		dto.setTitle(view.getTitle());
		dto.setUrl(view.getUrl());
		dto.setEditable(view.getEditable());
		dto.setIgnoreHistory(view.getIgnoreHistory());
		dto.setOptions(view.getOptions());
		return dto;
	}

	@Override
	public ScreenDTO getScreen(String name) {
		final Screen screen = metaRepository.getScreenByName(name);

		if (screen == null) {
			throw new ClientException(errorMessage("error.screen_not_found", name));
		}

		final List<String> views = bcUtils.getViews(screen.getName());
		final Map<String, Boolean> responsibilities = uiService.getResponsibilities();
		return getScreen(screen, new ScreenBuildMeta(views, responsibilities));
	}


	private ScreenDTO getScreen(Screen screen, ScreenBuildMeta meta) {
		final Map<String, List<ViewWidgets>> allViewWidgets = uiService.getAllWidgetsWithPositionByScreen(
				meta.getViews()
		);

		List<View> views = uiService.getViews(meta.getViews());

		final List<ViewDTO> viewDTOs = views.stream()
				.map(view -> buildViewDTO(view, allViewWidgets, meta.getResponsibilities()))
				.collect(Collectors.toList());

		final ScreenDTO result = toScreenDTO(screen);
		result.setNavigation(uiService.getScreenNavigation(screen));
		result.setViews(viewDTOs);
		result.setBo(getBusinessObject(viewDTOs));
		return result;
	}

	public ScreenDTO toScreenDTO(Screen screen) {
		ScreenDTO dto = new ScreenDTO();
		dto.setId(screen.getId().toString());
		dto.setName(screen.getName());
		dto.setTitle(screen.getTitle());
		dto.setPrimary(screen.getPrimary());
		dto.setPrimaries(screen.getPrimaries());
		return dto;
	}

	private BusinessObjectDTO getBusinessObject(List<ViewDTO> viewDTOs) {
		BusinessObjectDTO businessObjectDTO = new BusinessObjectDTO(
				viewDTOs.stream().map(ViewDTO::getWidgets)
						.flatMap(Collection::stream)
						.filter(widgetDTO -> Objects.nonNull(widgetDTO.getBcName()))
						.map(this::getWidgetBc)
						.flatMap(Collection::stream)
						.peek(this::setBcId)
						.distinct()
						.sorted(Comparator.comparing(BusinessComponentDTO::getUrl))
						.collect(Collectors.toList())
		);
		setBcParameters(businessObjectDTO);
		setFilterGroups(businessObjectDTO);
		return businessObjectDTO;
	}

	private void setBcId(BusinessComponentDTO dto) {
		BcDescription description = bcRegistry.getBcDescription(dto.getName());
		if (description != null) {
			//TODO>>used only for sql bc. Delete after refactoring
			Optional.ofNullable(description.getId()).ifPresent(dto::setId);
		}
	}

	private void setBcParameters(final BusinessObjectDTO boDto) {
		Map<String, BcProperties> defaultBcPropertiesMap = uiService.getStringDefaultBcPropertiesMap(boDto);
		boDto.getBc().forEach(dto -> {
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

				//TODO>>used only for sql bc. Delete after refactoring
				Optional.ofNullable(bcDescription.getBindsString()).ifPresent(dto::setBinds);
				Optional.ofNullable(bcDescription.getPageLimit()).ifPresent(dto::setLimit);
			}
		});
	}

	private void setFilterGroups(final BusinessObjectDTO boDto) {
		Map<String, List<FilterGroup>> filterGroupMap = uiService.getFilterGroups(boDto);
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
		var dto = new FilterGroupDTO();
		dto.setId(entity.getId().toString());
		dto.setName(entity.getName());
		dto.setFilters(entity.getFilters());
		dto.setBc(entity.getBc());
		return dto;
	}

	/**
	 * Gets a DB from a widget, taking into account whether widgets with a hierarchy have dependent BCS without their own widget
	 */
	@SneakyThrows
	private List<BusinessComponentDTO> getWidgetBc(WidgetDTO widgetDTO) {
		List<BusinessComponentDTO> result = new ArrayList<>();
		result.add(new BusinessComponentDTO(widgetDTO));
		Optional.ofNullable(widgetDTO.getOptions())
				.map(JsonUtils::readTree)
				.filter(JsonNode::isObject)
				.map(options -> JsonUtils.readValue(WidgetOptions.class, options))
				.map(WidgetOptions::getHierarchy)
				.ifPresent(list -> list.forEach(item -> {
							String bcName = item.getBcName();
							String url = bcRegistry.getUrlFromBc(bcName);
							result.add(new BusinessComponentDTO(new BcSourceBaseDTO(bcName, url)));
						}
				));
		return result;
	}

}
