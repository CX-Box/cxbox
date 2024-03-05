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
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.navigation.NavigationView;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UIServiceImpl {

	private final ResponsibilitiesService responsibilitiesService;

	private final CacheManager cacheManager;

	private final TransactionService txService;

	private final MetaRepository metaRepository;

	public Map<String, Boolean> getResponsibilities(IUser<Long> user, LOV userRole) {
		return responsibilitiesService.getListRespByUser(user, userRole);
	}

	public List<String> getViews(final String screenName, final IUser<Long> user, final LOV userRole) {
		final Set<String> responsibilities = getResponsibilities(user, userRole).keySet();
		final boolean getAll = Objects.equals(userRole, CoreDictionaries.InternalRole.ADMIN);
		return metaRepository.getViewByScreenAndResponsibilities(screenName, getAll, responsibilities).stream().map(NavigationView::getViewName).distinct().collect(Collectors.toList());
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
	public static class UserCache {

		private final UIServiceImpl uiService;

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
