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
import org.cxbox.meta.navigation.NavigationGroup;
import org.cxbox.meta.navigation.NavigationView;
import org.cxbox.meta.navigation.NavigationView_;
import org.cxbox.model.core.dao.JpaDao;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetaRepository {

	private final JpaDao jpaDao;

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
		jpaDao.delete(Bc.class, (root, query, cb) -> cb.and());
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


	public List<String> getAvailableScreenViews(String screenName, boolean getAll,
			Set<String> views) {
		return jpaDao.getList(NavigationView.class, (root, query, cb) -> cb.and(
				cb.equal(
						root.get(NavigationView_.screenName),
						screenName
				),
				cb.equal(
						root.get(NavigationView_.typeCd),
						ViewGroupType.NAVIGATION
				),
				getAll ? cb.and() : root.get(NavigationView_.viewName).in(views)
		)).stream().map(NavigationView::getViewName).distinct().collect(Collectors.toList());
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

	public List<Responsibilities> getResponsibilityByUserAndRole(IUser<Long> user, LOV userRole,
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
