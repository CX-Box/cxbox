/*
 * © OOO "SI IKS LAB", 2022-2024
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationUtils;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.IUser;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.meta.additionalFields.AdditionalFieldsDTO;
import org.cxbox.meta.data.FilterGroupDTO;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.entity.AdditionalFields;
import org.cxbox.meta.entity.AdditionalFields_;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.model.core.dao.JpaDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMetaProvider {


	private final ResponsibilitiesService responsibilitiesService;

	private final MetaRepository metaRepository;

	private final JpaDao jpaDao;

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper objectMapper;


	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
			cacheNames = CacheConfig.USER_CACHE,
			key = "{#root.methodName, #user.id, #userRole}"
	)
	public Map<String, ScreenDTO> getScreens(IUser<Long> user, LOV userRole) {
		Map<String, ScreenDTO> allScreens = metaRepository.getAllScreens();

		Map<String, ScreenDTO> allUserScreens = SerializationUtils.clone((HashMap<String, ScreenDTO>) allScreens);

		Map<String, Boolean> userViewToReadOnlyFlg = responsibilitiesService.getAvailableViews(user, userRole);
		allUserScreens.values().forEach(s -> {
			List<ViewDTO> userViews = s.getViews().stream()
					.filter(v -> v.getName() != null && userViewToReadOnlyFlg.containsKey(v.getName())).toList();
			s.setViews(userViews);
		});

		Map<String, List<FilterGroupDTO>> personalFilterGroups = metaRepository.getPersonalFilterGroups(user);
		allUserScreens.values()
				.forEach(s -> s.getBo().getBc()
						.forEach(bc -> bc.getFilterGroups()
								.addAll(personalFilterGroups.getOrDefault(bc.getName(), new ArrayList<>()))));

		List<AdditionalFieldsDTO> additionalFieldsDTO = getAdditionalFieldsDTO(user);
		allUserScreens.values()
				.forEach(s -> s.getViews()
						.forEach(v -> v.getWidgets()
								.forEach(w -> {
									var pers = additionalFieldsDTO.stream().filter(add -> v.getName().equals(add.getView()) && w.getName().equals(add.getWidget())).findFirst().orElse(null);
									w.setPersonalFields(pers);
								})));

		return allUserScreens;
	}


	private List<AdditionalFieldsDTO> getAdditionalFieldsDTO(IUser<Long> user) {

		return jpaDao.getList(
				AdditionalFields.class,
				(root, query, cb) ->
						cb.and(
								cb.equal(root.get(AdditionalFields_.userId), String.valueOf(user.getId()))
						)
		).stream().map(field -> {
			AdditionalFieldsDTO additionalFieldsDTO = new AdditionalFieldsDTO(field);
			additionalFieldsDTO.setAddedToAdditionalFields(getListFromJson(field.getAddedToAdditionalFields()));
			additionalFieldsDTO.setOrderFields(getListFromJson(field.getOrderFields()));
			additionalFieldsDTO.setRemovedFromAdditionalFields(getListFromJson(field.getAddedToAdditionalFields()));
			return additionalFieldsDTO;
		}).collect(Collectors.toList());
	}

	@SneakyThrows
	private List<String> getListFromJson(String json) {
		return objectMapper.readValue(json, List.class);
	}

}
