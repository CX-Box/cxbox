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

package org.cxbox.core.service.impl;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.dao.impl.ViewDAO;
import org.cxbox.core.dto.data.view.BcSourceBaseDTO;
import org.cxbox.core.dto.data.view.BusinessComponentDTO;
import org.cxbox.core.dto.data.view.BusinessObjectDTO;
import org.cxbox.core.dto.data.view.ScreenBuildMeta;
import org.cxbox.core.dto.data.view.ScreenDTO;
import org.cxbox.core.dto.data.view.ViewDTO;
import org.cxbox.core.dto.data.view.WidgetDTO;
import org.cxbox.core.dto.rowmeta.FilterGroupDTO;
import org.cxbox.core.exception.ClientException;
import org.cxbox.core.service.ViewService;
import org.cxbox.core.ui.model.json.WidgetOptions;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.ui.entity.BcProperties;
import org.cxbox.model.ui.entity.FilterGroup;
import org.cxbox.model.ui.entity.Screen;
import org.cxbox.model.ui.entity.Screen_;
import org.cxbox.model.ui.entity.View;
import org.cxbox.model.ui.entity.ViewWidgets;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ViewServiceImpl implements ViewService {

	private final JpaDao jpaDao;

	private final ViewDAO viewDAO;

	private final BcRegistry bcRegistry;

	private final UIServiceImpl uiService;

	private final SessionService sessionService;

	private ViewDTO buildViewDTO(View view,
			Map<String, List<ViewWidgets>> allViewWidgets,
			Map<String, Boolean> responsibilities) {
		int widgetIdCounter = 0;
		List<ViewWidgets> viewWidgetsList = allViewWidgets.get(view.getName());
		if (viewWidgetsList == null) {
			viewWidgetsList = Collections.emptyList();
		}
		ViewDTO result = new ViewDTO(view);
		result.setReadOnly(Optional.ofNullable(responsibilities.get(view.getName())).orElse(false));
		List<WidgetDTO> list = new ArrayList<>();
		for (ViewWidgets widgetWithPosition : viewWidgetsList) {
			WidgetDTO widgetDTO = new WidgetDTO(widgetWithPosition, widgetIdCounter);
			widgetDTO.setUrl(bcRegistry.getUrlFromBc(widgetWithPosition.getWidget().getBc()));
			list.add(widgetDTO);
			widgetIdCounter++;
		}
		result.setWidgets(list);
		return result;
	}

	@Override
	public ScreenDTO getScreen(String name) {
		final Screen screen = jpaDao.getSingleResultOrNull(
				Screen.class,
				(root, query, cb) -> cb.equal(root.get(Screen_.name), name)
		);

		if (screen == null) {
			throw new ClientException(errorMessage("error.screen_not_found", name));
		}

		final List<String> views = sessionService.getViews(screen.getName());
		final Map<String, Boolean> responsibilities = sessionService.getResponsibilities();
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

		final ScreenDTO result = new ScreenDTO(screen);
		result.setNavigation(uiService.getScreenNavigation(screen));
		result.setViews(viewDTOs);
		result.setBo(getBusinessObject(viewDTOs));
		return result;
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
					FilterGroupDTO filterGroupDTO = FilterGroupDTO.builder()
							.entity(fg)
							.build();
					result.add(filterGroupDTO);
				});
				dto.setFilterGroups(result);
			}
		});
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
