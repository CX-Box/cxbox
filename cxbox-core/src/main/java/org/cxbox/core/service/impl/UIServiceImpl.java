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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.dictionary.CoreDictionaries;
import org.cxbox.api.data.dictionary.CoreDictionaries.ViewGroupType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.core.dto.data.view.BusinessObjectDTO;
import org.cxbox.core.dto.data.view.ScreenNavigation;
import org.cxbox.core.dto.data.view.ScreenNavigation.MenuItem;
import org.cxbox.core.dto.data.view.ScreenNavigation.SingleView;
import org.cxbox.core.dto.data.view.ScreenNavigation.ViewGroup;
import org.cxbox.core.dto.data.view.ScreenResponsibility;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.core.service.UIService;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.core.entity.User;
import org.cxbox.model.ui.entity.BcProperties;
import org.cxbox.model.ui.entity.BcProperties_;
import org.cxbox.model.ui.entity.FilterGroup;
import org.cxbox.model.ui.entity.FilterGroup_;
import org.cxbox.model.ui.entity.Screen;
import org.cxbox.model.ui.entity.Screen_;
import org.cxbox.model.ui.entity.View;
import org.cxbox.model.ui.entity.ViewWidgets;
import org.cxbox.model.ui.entity.ViewWidgets_;
import org.cxbox.model.ui.entity.View_;
import org.cxbox.model.ui.navigation.NavigationGroup;
import org.cxbox.model.ui.navigation.NavigationGroup_;
import org.cxbox.model.ui.navigation.NavigationView;
import org.cxbox.model.ui.navigation.NavigationView_;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UIServiceImpl implements UIService {

	@Qualifier("cxboxObjectMapper")
	private final ObjectMapper objectMapper;

	private final ResponsibilitiesService responsibilitiesService;

	private final UserRoleService userRoleService;

	private final JpaDao jpaDao;

	private final UICache uiCache;

	private final CacheManager cacheManager;

	private final TransactionService txService;

	private JsonNode defaultUserSettings;

	private List<ScreenResponsibility> commonScreens;

	//TODO>>iborisenko>>refactor to constructor. Make commonScreens, defaultUserSettings final to obviously indicate their thread safety
	@SneakyThrows
	@PostConstruct
	protected void init() {
		InputStream userSettings = getClass().getResourceAsStream("/userSettings.json");
		if (userSettings != null) {
			defaultUserSettings = objectMapper.readTree(userSettings);
		}
		InputStream screens = getClass().getResourceAsStream("/commonScreens.json");
		if (screens != null) {
			commonScreens = objectMapper.readValue(screens, ScreenResponsibility.LIST_TYPE_REFERENCE);
		} else {
			commonScreens = new ArrayList<>();
		}
	}

	@Override
	public List<ScreenResponsibility> getCommonScreens() {
		return commonScreens;
	}

	@Override
	public boolean isCommonScreen(String screenName) {
		List<ScreenResponsibility> commonScreens = getCommonScreens();
		for (ScreenResponsibility screen : commonScreens) {
			if (screenName.equals(screen.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get custom settings
	 *
	 * @return JsonNode
	 */
	public JsonNode getUserSettings() {
		return defaultUserSettings;
	}

	@Override
	public Map<String, Boolean> getResponsibilities(User user, LOV userRole) {
		return responsibilitiesService.getListRespByUser(user, userRole);
	}

	@Override
	public String getFirstViewFromResponsibilities(User user, LOV userRole, String... views) {
		Set<String> responsibilities = getResponsibilities(user, userRole).keySet();
		if (responsibilities.isEmpty() && views.length > 0) {
			return views[0];
		}
		for (String view : views) {
			if (responsibilities.contains(view)) {
				return view;
			}
		}
		return null;
	}

	@Override
	public String getFirstViewFromResponsibilities(User user, String... views) {
		return getFirstViewFromResponsibilities(user, userRoleService.getMainUserRoleKey(user), views);
	}

	@Override
	public List<String> getViews(final String screenName, final User user, final LOV userRole) {
		final Set<String> responsibilities = getResponsibilities(user, userRole).keySet();
		final boolean getAll = Objects.equals(userRole, CoreDictionaries.InternalRole.ADMIN) || isCommonScreen(screenName);
		return jpaDao.getList(NavigationView.class, (root, query, cb) -> cb.and(
				cb.equal(
						root.get(NavigationView_.screenName),
						screenName
				),
				cb.equal(
						root.get(NavigationView_.typeCd),
						CoreDictionaries.ViewGroupType.NAVIGATION
				),
				getAll ? cb.and() : root.get(NavigationView_.viewName).in(responsibilities)
		)).stream().map(NavigationView::getViewName).distinct().collect(Collectors.toList());
	}

	public Map<String, BcProperties> getStringDefaultBcPropertiesMap(BusinessObjectDTO boDto) {
		Map<String, BcProperties> result = new HashMap<>(boDto.getBc().size());
		Map<String, BcProperties> allProperties = uiCache.getBcProperties();
		boDto.getBc().forEach(bc -> result.put(bc.getName(), allProperties.get(bc.getName())));
		return result;
	}

	public Map<String, List<FilterGroup>> getFilterGroups(BusinessObjectDTO boDto) {
		HashMap<String, List<FilterGroup>> result = new HashMap<>(boDto.getBc().size());
		Map<String, List<FilterGroup>> all = uiCache.getFilterGroups();
		boDto.getBc().forEach(bc -> result.put(bc.getName(), Optional.ofNullable(all.get(bc.getName())).map(ArrayList::new).orElse(null)));
		return result;
	}

	public Optional<Map<String, List<FilterGroup>>> getPersonalFilterGroups(BusinessObjectDTO boDto, User user) {
		HashMap<String, List<FilterGroup>> result = new HashMap<>(boDto.getBc().size());

		Map<String, List<FilterGroup>> all = jpaDao.getList(FilterGroup.class, (root, cq, cb) ->
				cb.and(
						cb.isNotNull(root.get(FilterGroup_.bc)),
						cb.equal(root.get(FilterGroup_.user), user)
				)
		).stream().collect(
				Collectors.groupingBy(FilterGroup::getBc)
		);

		boDto.getBc().forEach(bc -> result.put(bc.getName(), all.get(bc.getName())));
		return Optional.of(result);
	}


	public Map<String, List<ViewWidgets>> getAllWidgetsWithPositionByScreen(final List<String> views) {
		HashMap<String, List<ViewWidgets>> result = new HashMap<>(views.size());
		Map<String, List<ViewWidgets>> all = uiCache.getWidgets();
		views.forEach(view -> result.put(view, all.get(view)));
		return result;
	}

	public List<View> getViews(final List<String> views) {
		List<View> result = new ArrayList<>(views.size());
		Map<String, View> allViews = uiCache.getViews();
		views.forEach(view -> result.add(allViews.get(view)));
		return result;
	}

	public ScreenNavigation getScreenNavigation(final Screen screen) {
		return uiCache.getScreenNavigation(screen);
	}

	public Screen findScreenByName(String name) {
		return jpaDao.getSingleResultOrNull(Screen.class, (root, cq, cb) -> cb.equal(root.get(Screen_.name), name));
	}

	@Override
	public void invalidateCache() {
		txService.invokeAfterCompletion(Invoker.of(
				() -> cacheManager.getCache(CacheConfig.UI_CACHE).clear()
		));
	}

	@Component
	@RequiredArgsConstructor
	public static class UICache {

		private final JpaDao jpaDao;

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.methodName}"
		)
		public Map<String, List<ViewWidgets>> getWidgets() {
			return jpaDao.getList(ViewWidgets.class, (root, cq, cb) -> {
				root.fetch(ViewWidgets_.widget);
				return cb.isNotNull(root.get(ViewWidgets_.viewName));
			}).stream().collect(
					Collectors.groupingBy(ViewWidgets::getViewName)
			);
		}

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.methodName}"
		)
		public Map<String, View> getViews() {
			return jpaDao.getList(View.class, (root, cq, cb) ->
					cb.isNotNull(root.get(View_.name))
			).stream().collect(
					Collectors.toMap(View::getName, Function.identity())
			);
		}

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.methodName}"
		)
		public Map<String, BcProperties> getBcProperties() {
			return jpaDao.getList(BcProperties.class, (root, cq, cb) ->
					cb.isNotNull(root.get(BcProperties_.bc))
			).stream().collect(Collectors.toMap(BcProperties::getBc, Function.identity()));
		}


		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.methodName}"
		)
		public Map<String, List<FilterGroup>> getFilterGroups() {
			Map<String, List<FilterGroup>> mapOfFilterGroup = jpaDao.getList(FilterGroup.class, (root, cq, cb) ->
					cb.and(
							cb.isNotNull(root.get(FilterGroup_.bc)),
							cb.isNull(root.get(FilterGroup_.user)
							)
					)).stream().collect(
					Collectors.groupingBy(FilterGroup::getBc)
			);
			return mapOfFilterGroup;
		}

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.methodName, #screen.name}"
		)
		public ScreenNavigation getScreenNavigation(final Screen screen) {

			final List<NavigationGroup> groups = jpaDao.getList(NavigationGroup.class, (root, query, cb) -> {
				query.orderBy(cb.asc(root.get(NavigationGroup_.seq)));
				return cb.and(
						cb.equal(root.get(NavigationGroup_.screenName), screen.getName()),
						cb.equal(root.get(NavigationGroup_.typeCd), ViewGroupType.NAVIGATION)
				);
			});

			final List<NavigationView> views = jpaDao.getList(NavigationView.class, (root, query, cb) -> {
				query.orderBy(cb.asc(root.get(NavigationView_.seq)));
				return cb.and(
						cb.equal(root.get(NavigationView_.screenName), screen.getName()),
						cb.equal(root.get(NavigationView_.typeCd), ViewGroupType.NAVIGATION)
				);
			});

			final List<MenuItem> firstLevelMenu = new ArrayList<>();
			final Map<String, MenuItem> map = new HashMap<>();
			for (final NavigationGroup navigationGroup : groups) {
				ScreenNavigation.ViewGroup viewGroup = (ViewGroup) map.computeIfAbsent(
						navigationGroup.getId(),
						key -> new ScreenNavigation.ViewGroup()
				);
				viewGroup.setId(navigationGroup.getId());
				viewGroup.setHidden(navigationGroup.getHidden());
				viewGroup.setTitle(navigationGroup.getTitle());
				viewGroup.setDefaultView(navigationGroup.getDefaultView());
				viewGroup.setSeq(navigationGroup.getSeq());
				if (navigationGroup.getParent() == null) {
					firstLevelMenu.add(
							navigationGroup.getSeq() > firstLevelMenu.size() ? firstLevelMenu.size() : navigationGroup.getSeq(),
							viewGroup
					);
				} else {
					final ScreenNavigation.ViewGroup parentGroup = (ViewGroup) map.computeIfAbsent(
							navigationGroup.getParent().getId(),
							key -> new ViewGroup()
					);
					if (parentGroup.getChild() == null) {
						parentGroup.setChild(new ArrayList<>());
					}
					final List<MenuItem> childList = parentGroup.getChild();
					childList.add(
							navigationGroup.getSeq() > childList.size() ? childList.size() : navigationGroup.getSeq() - 1,
							viewGroup
					);
				}
			}

			for (final NavigationView view : views) {
				final SingleView singleView = new SingleView();
				singleView.setViewName(view.getViewName());
				singleView.setHidden(view.getHidden());
				singleView.setId(view.getId());
				singleView.setSeq(view.getSeq());
				if (view.getParentGroup() == null) {
					firstLevelMenu.add(view.getSeq() > firstLevelMenu.size() ? firstLevelMenu.size() : view.getSeq(), singleView);
				} else {
					final ScreenNavigation.ViewGroup parentGroup = (ViewGroup) map.get(view.getParentGroup().getId());
					if (parentGroup.getChild() == null) {
						parentGroup.setChild(new ArrayList<>());
					}
					final List<MenuItem> childList = parentGroup.getChild();
					childList.add(view.getSeq() > childList.size() ? childList.size() : view.getSeq() - 1, singleView);
				}
			}
			final ScreenNavigation screenNavigation = new ScreenNavigation();
			screenNavigation.setMenu(firstLevelMenu);

			//TODO>>we use sort now, so we should remove complex unnecessary logic like "iew.getSeq() > childList.size() ? childList.size() : view.getSeq() - 1"
			sortMenuItemsBySeq(screenNavigation);
			return screenNavigation;
		}

		@CacheEvict(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = CacheConfig.UI_CACHE, allEntries = true)
		public void evict() {
		}

	}

	private static void sortMenuItemsBySeq(ScreenNavigation screenNavigation) {
		screenNavigation.getMenu().sort(Comparator.comparingInt(MenuItem::getSeq));
		screenNavigation.getMenu().forEach(UIServiceImpl::recursiveSort);
	}

	private static void recursiveSort(MenuItem m) {
		if (m instanceof ViewGroup) {
			final List<MenuItem> child = ((ViewGroup) m).getChild();
			child.sort(Comparator.comparingInt(MenuItem::getSeq));
			child.forEach(UIServiceImpl::recursiveSort);
		}
	}


}
