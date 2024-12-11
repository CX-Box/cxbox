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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.cxbox.api.ScreenResponsibilityService;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.service.session.IUser;
import org.cxbox.core.service.ResponsibilitiesService;
import org.cxbox.dto.ScreenResponsibility;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.entity.ResponsibilitiesAction;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.metahotreload.repository.UserMetaProvider;
import org.cxbox.model.core.dao.JpaDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScreenResponsibilityServiceImpl implements ScreenResponsibilityService {

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper objectMapper;

	private final ResponsibilitiesService respService;

	private final UserMetaProvider userMetaProvider;

	private final JpaDao jpaDao;

	private final MetaConfigurationProperties metaConfigurationProperties;

	/**
	 * Get all available screens with respect of user role
	 *
	 * @param user Active session user
	 * @param userRole User role
	 * @return JsonNode Available screens
	 */
	@Override
	public List<ScreenResponsibility> getScreens(IUser<Long> user, Set<String> userRole) {
		Map<String, ScreenResponsibility> allUserScreens = userMetaProvider.getAvailableScreensResponsibilities(
				user,
				userRole
		);
		screenOverride(user, userRole, allUserScreens);
		widgetActionGroupsOverride(user, userRole, allUserScreens);
		return allUserScreens.values().stream()
				.sorted(Comparator.comparing(ScreenResponsibility::getOrder).thenComparing(ScreenResponsibility::getName))
				.toList();
	}

	public void screenOverride(IUser<Long> user, Set<String> userRole, Map<String, ScreenResponsibility> allUserScreens) {
		var allOverrides = respService.getOverrideScreensResponsibilities(user, userRole);
		allOverrides.forEach(override -> allUserScreens.computeIfPresent(
						override.getName(),
						(key, old) -> new ScreenResponsibility()
								.setId(old.getId())
								.setUrl(old.getUrl())
								.setMeta(old.getMeta())
								.setName(override.getName())
								.setOrder(Optional.ofNullable(override.getOrder()).orElse(Optional.ofNullable(old.getOrder()).orElse(0)))
								.setText(Optional.ofNullable(override.getText()).orElse(old.getText()))
								.setIcon(Optional.ofNullable(override.getIcon()).orElse(old.getIcon()))
				)
		);
	}

	public void widgetActionGroupsOverride(IUser<Long> user, Set<String> userRole, Map<String, ScreenResponsibility> allUserScreens) {
		var roleAction = jpaDao.getList(ResponsibilitiesAction.class);
		if (!metaConfigurationProperties.isWidgetActionGroupsEnabled()) {
			allUserScreens.values().stream().map(s -> ((ScreenDTO) s.getMeta())).forEach(sc -> sc.getViews()
					.forEach(v -> {
						v.setWidgets(v.getWidgets().stream().map(SerializationUtils::clone).toList());
						v.getWidgets().forEach(w -> {
							try {
								ObjectNode widgetJson = objectMapper.readValue(objectMapper.writeValueAsString(w), ObjectNode.class);
								ObjectNode optionsNode = getObjectPropOrElseCreate(widgetJson, WidgetSourceDTO.OPTIONS_PROP);
								ObjectNode actionsGroups = getObjectPropReCreate(optionsNode, WidgetSourceDTO.ACTION_GROUPS_PROP);
								ArrayNode include = getArrayPropOrElseCreate(actionsGroups,  WidgetSourceDTO.INCLUDE_PROP);
								roleAction.stream()
										.filter(ra -> ra.isAvailable(userRole, v.getName(), w.getName()))
										.map(ResponsibilitiesAction::getAction)
										.forEach(include::add);
								w.setOptions(objectMapper.writeValueAsString(optionsNode));
							} catch (JsonProcessingException e) {
								throw new IllegalStateException(e);
							}
						});
					}));
		}
	}

	private ObjectNode getObjectPropOrElseCreate(ObjectNode parent, String propName) {
		JsonNode optionsNode = parent.get(propName);
		if (optionsNode instanceof ObjectNode objectNode) {
			return objectNode;
		} else {
			parent.set(propName, objectMapper.createObjectNode());
			return (ObjectNode) parent.get(propName);
		}
	}

	private ObjectNode getObjectPropReCreate(ObjectNode parent, String propName) {
		parent.set(propName, objectMapper.createObjectNode());
		return (ObjectNode) parent.get(propName);
	}

	private ArrayNode getArrayPropOrElseCreate(ObjectNode parent, String propName) {
		JsonNode optionsNode = parent.get(propName);
		if (optionsNode instanceof ArrayNode objectNode) {
			return objectNode;
		} else {
			parent.set(propName, objectMapper.createArrayNode());
			return (ArrayNode) parent.get(propName);
		}
	}
}

