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
import org.apache.commons.lang3.StringUtils;
import org.cxbox.api.data.dictionary.CoreDictionaries;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.IUser;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.meta.entity.Responsibilities;
import org.cxbox.meta.entity.Responsibilities.ResponsibilityType;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;

@RequiredArgsConstructor
public class ResponsibilitiesServiceImpl implements ResponsibilitiesService {

	private final MetaRepository metaRepository;

	private final CacheManager cacheManager;

	private final TransactionService txService;

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER, cacheNames = {
			CacheConfig.REQUEST_CACHE}, key = "{#root.methodName, #user.id, #userRole}")
	public Map<String, Boolean> getAvailableViews(IUser<Long> user, LOV userRole) {
		return metaRepository.getResponsibilityByUserAndRole(user, userRole, ResponsibilityType.VIEW)
				.stream()
				.collect(
						Collectors.toMap(
								Responsibilities::getView,
								Responsibilities::isReadOnly,
								(x1, x2) -> x2
						)
				);
	}

	public String getAvailableScreens(IUser<Long> user, LOV userRole) {
		return metaRepository.getResponsibilityByUserAndRole(user, userRole, ResponsibilityType.SCREEN)
				.stream()
				.map(Responsibilities::getScreens)
				.filter(StringUtils::isNotBlank)
				.findFirst()
				.orElse(null);
	}

	public void invalidateCache() {
		txService.invokeAfterCompletion(Invoker.of(
				() -> {
					cacheManager.getCache(CacheConfig.UI_CACHE).clear();
					cacheManager.getCache(CacheConfig.USER_CACHE).clear();
				}
		));
	}

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
			cacheNames = {CacheConfig.USER_CACHE},
			key = "{#root.methodName, #screenName, #user.id, #userRole}"
	)
	public List<String> getAvailableScreenViews(String screenName, IUser<Long> user, LOV userRole) {
		final Set<String> availableViews = this.getAvailableViews(user, userRole).keySet();
		final boolean getAll = Objects.equals(userRole, CoreDictionaries.InternalRole.ADMIN);
		return metaRepository.getAvailableScreenViews(screenName, getAll, availableViews);
	}

}
