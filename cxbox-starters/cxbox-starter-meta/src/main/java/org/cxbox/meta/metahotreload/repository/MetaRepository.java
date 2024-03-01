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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.data.dictionary.CoreDictionaries.ViewGroupType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.IUser;
import org.cxbox.meta.data.FilterGroupDTO;
import org.cxbox.meta.entity.Bc;
import org.cxbox.meta.entity.BcProperties;
import org.cxbox.meta.entity.BcProperties_;
import org.cxbox.meta.entity.FilterGroup;
import org.cxbox.meta.entity.FilterGroup_;
import org.cxbox.meta.entity.Responsibilities;
import org.cxbox.meta.entity.Responsibilities.ResponsibilityType;
import org.cxbox.meta.entity.Responsibilities_;
import org.cxbox.meta.entity.Screen;
import org.cxbox.meta.entity.Screen_;
import org.cxbox.meta.entity.View;
import org.cxbox.meta.entity.ViewWidgets;
import org.cxbox.meta.entity.ViewWidgets_;
import org.cxbox.meta.entity.View_;
import org.cxbox.meta.entity.Widget;
import org.cxbox.meta.entity.Widget_;
import org.cxbox.meta.navigation.NavigationGroup;
import org.cxbox.meta.navigation.NavigationGroup_;
import org.cxbox.meta.navigation.NavigationView;
import org.cxbox.meta.navigation.NavigationView_;
import org.cxbox.model.core.dao.JpaDao;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetaRepository {

	private final JpaDao jpaDao;

	public void saveScreen(Screen screen) {
		jpaDao.save(screen);
	}

	public void saveView(View view) {
		jpaDao.save(view);
	}

	public void saveViewWidget(ViewWidgets viewWidget) {
		jpaDao.save(viewWidget);
	}

	public void saveAllWidgets(Map<String, Widget> nameToWidget) {
		nameToWidget.forEach((name, widget) -> jpaDao.save(widget));
	}

	public void saveBc(Bc bc) {
		jpaDao.save(bc);
	}

	public void saveNavigationGroup(NavigationGroup navigationGroup) {
		jpaDao.save(navigationGroup);
	}

	public void saveNavigationView(NavigationView navigationView) {
		jpaDao.save(navigationView);
	}

	public void deleteAndSaveResponsibilities(List<Responsibilities> responsibilities) {
		jpaDao.delete(Responsibilities.class, (root, query, cb) -> cb.and());
		jpaDao.saveAll(responsibilities);
	}


	public List<NavigationView> getNavigationViews() {
		return jpaDao.getList(NavigationView.class);
	}

	public void deleteAllMeta() {
		jpaDao.delete(NavigationView.class, (root, query, cb) -> cb.and());
		jpaDao.delete(NavigationGroup.class, (root, query, cb) -> cb.and());
		jpaDao.delete(Screen.class, (root, query, cb) -> cb.and());
		jpaDao.delete(ViewWidgets.class, (root, query, cb) -> cb.and());
		jpaDao.delete(View.class, (root, query, cb) -> cb.and());
		jpaDao.delete(Widget.class, (root, query, cb) -> cb.and());
		jpaDao.delete(Bc.class, (root, query, cb) -> cb.and());
	}


	public Screen getScreenByName(String name) {
		return jpaDao.getSingleResultOrNull(
				Screen.class,
				(root, query, cb) -> cb.equal(root.get(Screen_.name), name)
		);
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


	public List<NavigationView> getViewByScreenAndResponsibilities(String screenName, boolean getAll,
			Set<String> responsibilities) {
		return jpaDao.getList(NavigationView.class, (root, query, cb) -> cb.and(
				cb.equal(
						root.get(NavigationView_.screenName),
						screenName
				),
				cb.equal(
						root.get(NavigationView_.typeCd),
						ViewGroupType.NAVIGATION
				),
				getAll ? cb.and() : root.get(NavigationView_.viewName).in(responsibilities)
		));
	}

	public Widget getWidgetById(Long widgetId) {
		return jpaDao.findById(Widget.class, widgetId);
	}

	public List<Long> getWidgetByViewName(String viewName) {
		return jpaDao.getList(
				ViewWidgets.class,
				Long.class,
				(root, cb) -> root.get(ViewWidgets_.widget).get(Widget_.id),
				(root, query, cb) -> cb.equal(root.get(ViewWidgets_.viewName), viewName)
		);
	}

	public List<Long> getBcWidgets(String bc) {
		return jpaDao.getList(
				Widget.class,
				Long.class,
				(root, cb) -> root.get(Widget_.id),
				(root, query, cb) -> cb.equal(root.get(Widget_.bc), bc)
		);
	}

	public List<String> getWidget(Long widgetId) {
		return jpaDao.getList(
				ViewWidgets.class,
				String.class,
				(root, cb) -> root.get(ViewWidgets_.viewName),
				(root, query, cb) -> {
					query.distinct(true);
					return cb.equal(root.get(ViewWidgets_.widget).get(Widget_.id), widgetId);
				}
		);
	}

	public List<NavigationGroup> getScreenNavigationGroups(Screen screen) {
		return jpaDao.getList(NavigationGroup.class, (root, query, cb) -> {
			query.orderBy(cb.asc(root.get(NavigationGroup_.seq)));
			return cb.and(
					cb.equal(root.get(NavigationGroup_.screenName), screen.getName()),
					cb.equal(root.get(NavigationGroup_.typeCd), ViewGroupType.NAVIGATION)
			);
		});
	}


	public List<NavigationView> getScreenViews(Screen screen) {
		return jpaDao.getList(NavigationView.class, (root, query, cb) -> {
			query.orderBy(cb.asc(root.get(NavigationView_.seq)));
			return cb.and(
					cb.equal(root.get(NavigationView_.screenName), screen.getName()),
					cb.equal(root.get(NavigationView_.typeCd), ViewGroupType.NAVIGATION)
			);
		});
	}

	public Map<String, List<ViewWidgets>> getWidgets() {
		return jpaDao.getList(ViewWidgets.class, (root, cq, cb) -> {
			root.fetch(ViewWidgets_.widget);
			return cb.isNotNull(root.get(ViewWidgets_.viewName));
		}).stream().collect(
				Collectors.groupingBy(ViewWidgets::getViewName)
		);
	}

	public Map<String, View> getViews() {
		return jpaDao.getList(View.class, (root, cq, cb) ->
				cb.isNotNull(root.get(View_.name))
		).stream().collect(
				Collectors.toMap(View::getName, Function.identity())
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

	public List<Responsibilities> getListByUserList(IUser<Long> user, LOV userRole,
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


}
