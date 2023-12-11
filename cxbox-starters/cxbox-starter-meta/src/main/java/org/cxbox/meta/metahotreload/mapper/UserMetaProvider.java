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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.IUser;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.data.ViewDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMetaProvider {

	private final MetaProvider metaProvider;

	private final ResponsibilitiesService responsibilitiesService;

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
			cacheNames = CacheConfig.USER_CACHE,
			key = "{#root.methodName}"
	)
	public Map<String, ScreenDTO> getScreens(IUser<Long> user, LOV userRole) {
		Map<String, ScreenDTO> allScreens = metaProvider.getAllScreens();
		Map<String, ScreenDTO> allUserScreens = SerializationUtils.clone((HashMap<String, ScreenDTO>) allScreens);
		Map<String, Boolean> userViewToReadOnlyFlg = responsibilitiesService.getListRespByUser(user, userRole);
		allUserScreens.values().forEach(s -> {
			List<ViewDTO> userViews = s.getViews().stream().filter(v -> v.getName() != null && userViewToReadOnlyFlg.containsKey(v.getName())).toList();
			s.setViews(userViews);
		});
		return allUserScreens;
	}


}
