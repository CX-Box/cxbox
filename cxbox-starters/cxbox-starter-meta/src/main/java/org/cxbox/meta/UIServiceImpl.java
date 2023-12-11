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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.data.dictionary.CoreDictionaries;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.IUser;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.meta.data.BusinessObjectDTO;
import org.cxbox.meta.data.ScreenNavigation;
import org.cxbox.meta.data.ScreenNavigation.MenuItem;
import org.cxbox.meta.data.ScreenNavigation.SingleView;
import org.cxbox.meta.data.ScreenNavigation.ViewGroup;
import org.cxbox.meta.entity.BcProperties;
import org.cxbox.meta.entity.FilterGroup;
import org.cxbox.meta.entity.Screen;
import org.cxbox.meta.entity.View;
import org.cxbox.meta.entity.ViewWidgets;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.navigation.NavigationGroup;
import org.cxbox.meta.navigation.NavigationView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UIServiceImpl {

	@Qualifier("cxboxObjectMapper")
	private final ObjectMapper objectMapper;

	private final ResponsibilitiesService responsibilitiesService;

	private final UICache uiCache;

	private final CacheManager cacheManager;

	private final TransactionService txService;

	private final MetaRepository metaRepository;

	private final SessionService sessionService;

	public Map<String, Boolean> getResponsibilities() {
		return getResponsibilities(sessionService.getSessionUser(), sessionService.getSessionUserRole());
	}

	public Map<String, Boolean> getResponsibilities(IUser<Long> user, LOV userRole) {
		return responsibilitiesService.getListRespByUser(user, userRole);
	}

	public List<String> getViews(final String screenName, final IUser<Long> user, final LOV userRole) {
		final Set<String> responsibilities = getResponsibilities(user, userRole).keySet();
		final boolean getAll = Objects.equals(userRole, CoreDictionaries.InternalRole.ADMIN);
		return metaRepository.getViewByScreenAndResponsibilities(screenName, getAll, responsibilities).stream().map(NavigationView::getViewName).distinct().collect(Collectors.toList());
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
		boDto.getBc().forEach(bc -> result.put(bc.getName(), all.get(bc.getName())));
		return result;
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


	public void invalidateCache() {
		txService.invokeAfterCompletion(Invoker.of(
				() -> {
					cacheManager.getCache(CacheConfig.UI_CACHE).clear();
					cacheManager.getCache(CacheConfig.USER_CACHE).clear();
				}
		));
	}

	@Component
	@RequiredArgsConstructor
	public static class UICache {

		private final MetaRepository metaRepository;

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.methodName}"
		)
		public Map<String, List<ViewWidgets>> getWidgets() {
			return metaRepository.getWidgets();
		}

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.methodName}"
		)
		public Map<String, View> getViews() {
			return metaRepository.getViews();
		}

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.methodName}"
		)
		public Map<String, BcProperties> getBcProperties() {
			return metaRepository.getBcProperties();
		}

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.methodName}"
		)
		public Map<String, List<FilterGroup>> getFilterGroups() {
			return metaRepository.getFilterGroups();
		}

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = CacheConfig.UI_CACHE,
				key = "{#root.methodName, #screen.name}"
		)
		public ScreenNavigation getScreenNavigation(final Screen screen) {

			final List<NavigationGroup> groups = metaRepository.getScreenNavigationGroups(screen);

			final List<NavigationView> views = metaRepository.getScreenViews(screen);

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


	@Component
	@RequiredArgsConstructor
	public static class UserCache {

		private final UIServiceImpl uiService;

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = {CacheConfig.USER_CACHE},
				key = "{#root.methodName, #user.id, #userRole}"
		)
		public Map<String, Boolean> getResponsibilities(final IUser<Long> user, final LOV userRole) {
			return uiService.getResponsibilities(
					user,
					userRole
			);
		}

		@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
				cacheNames = {CacheConfig.USER_CACHE},
				key = "{#root.methodName, #screenName, #user.id, #userRole}"
		)
		public List<String> getViews(final String screenName, final IUser<Long> user, final LOV userRole) {
			return uiService.getViews(
					screenName,
					user,
					userRole
			);
		}

	}

}
