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

package org.cxbox.meta.metahotreload.repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.service.session.IUser;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.dto.ScreenResponsibility;
import org.cxbox.meta.data.FilterGroupDTO;
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.entity.BcProperties;
import org.cxbox.meta.entity.BcProperties_;
import org.cxbox.meta.entity.FilterGroup;
import org.cxbox.meta.entity.FilterGroup_;
import org.cxbox.meta.entity.Responsibilities;
import org.cxbox.meta.entity.Responsibilities.ResponsibilityType;
import org.cxbox.meta.entity.Responsibilities_;
import org.cxbox.meta.metahotreload.dto.BcSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.metahotreload.mapper.ScreenMapper;
import org.cxbox.meta.metahotreload.mapper.ViewMapper;
import org.cxbox.meta.metahotreload.service.MetaResourceReaderService;
import org.cxbox.model.core.dao.JpaDao;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetaRepository {

	private final JpaDao jpaDao;

	private final MetaResourceReaderService metaResourceReaderService;

	private final ScreenMapper screenMapper;

	private final ViewMapper viewMapper;

	public List<BcSourceDTO> getBcs() {
		return metaResourceReaderService.getBcs();
	}

	public void deleteAndSaveResponsibilities(List<Responsibilities> responsibilities) {
		jpaDao.delete(Responsibilities.class, (root, query, cb) -> cb.and());
		jpaDao.saveAll(responsibilities);
	}

	public void deleteAllMeta() {
	}

	/*
	bc to List of Personal Filter Groups
	 */
	public Map<String, List<FilterGroupDTO>> getPersonalFilterGroups(IUser<Long> user) {
		return jpaDao.getList(FilterGroup.class, (root, cq, cb) ->
				cb.and(
						cb.isNotNull(root.get(FilterGroup_.bc)),
						cb.equal(root.get(FilterGroup_.userId), String.valueOf(user.getId()))
				)
		).stream().map(fg -> FilterGroupDTO.builder()
				.bc(fg.getBc())
				.personal(Boolean.TRUE)
				.id(String.valueOf(fg.getId()))
				.name(fg.getName())
				.filters(fg.getFilters())
				.build()
		).collect(
				Collectors.groupingBy(FilterGroupDTO::getBc)
		);
	}


	public Map<String, BcProperties> getBcProperties() {
		return jpaDao.getList(BcProperties.class, (root, cq, cb) ->
				cb.isNotNull(root.get(BcProperties_.bc))
		).stream().collect(Collectors.toMap(BcProperties::getBc, Function.identity()));
	}


	public Map<String, List<FilterGroup>> getFilterGroups() {
		return jpaDao.getList(FilterGroup.class, (root, cq, cb) ->
				cb.and(
						cb.isNotNull(root.get(FilterGroup_.bc)),
						cb.isNull(root.get(FilterGroup_.userId)
						)
				)).stream().collect(
				Collectors.groupingBy(FilterGroup::getBc)
		);
	}

	public List<Responsibilities> getResponsibilityByUserAndRole(IUser<Long> user, String userRole,
			ResponsibilityType responsibilityType) {
		// В листе может быть не более одной записи

		return jpaDao.getList(
				Responsibilities.class,
				(root, cq, cb) -> cb.and(
						cb.equal(root.get(Responsibilities_.departmentId), user.getDepartmentId()),
						cb.equal(root.get(Responsibilities_.internalRoleCD), userRole),
						cb.equal(root.get(Responsibilities_.responsibilityType), responsibilityType)
				)
		);
	}


	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
			cacheNames = CacheConfig.UI_CACHE,
			key = "{#root.methodName}"
	)
	public Map<String, ScreenResponsibility> getAllScreens() {
		//load data
		var screens = metaResourceReaderService.getScreens();
		var widgets = metaResourceReaderService.getWidgets();
		var views = metaResourceReaderService.getViews();
		Map<String, BcProperties> bcProps = getBcProperties();
		Map<String, List<FilterGroup>> filterGroups = getFilterGroups();

		//map data
		var widgetNameToWidget = widgets.stream()
				.collect(Collectors.toMap(WidgetSourceDTO::getName, e -> e));
		var viewNameToView = views
				.stream()
				.map(v -> viewMapper.map(v, widgetNameToWidget))
				.collect(Collectors.toMap(ViewDTO::getName, e -> e));
		return screens.stream()
				.map(screenSourceDto -> screenMapper.map(screenSourceDto, viewNameToView, bcProps, filterGroups))
				.collect(Collectors.toMap(ScreenResponsibility::getName, e -> e));
	}

}
