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

package org.cxbox.meta.metahotreload.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.InternalAuthorizationService;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.MetaHotReloadService;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.dto.BcSourceDTO;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.entity.Responsibilities;
import org.cxbox.meta.entity.Responsibilities.ResponsibilityType;
import org.cxbox.meta.navigation.NavigationView;

@RequiredArgsConstructor
public class MetaHotReloadServiceImpl implements MetaHotReloadService {

	protected final MetaConfigurationProperties config;

	protected final MetaResourceReaderService metaResourceReaderService;

	protected final InternalAuthorizationService authzService;

	protected final TransactionService txService;

	protected final MetaRepository metaRepository;

	protected final ScreenAndNavigationGroupAndNavigationViewUtil screenAndNavigationGroupAndNavigationViewUtil;

	protected final BcUtil bcUtil;


	public void loadMeta() {
		//TODO>>new metalock
		List<BcSourceDTO> bcDtos = metaResourceReaderService.getBcs();
		List<ScreenSourceDto> screenDtos = metaResourceReaderService.getScreens();
		List<WidgetSourceDTO> widgetDtos = metaResourceReaderService.getWidgets();
		List<ViewSourceDTO> viewDtos = metaResourceReaderService.getViews();

		authzService.loginAs(authzService.createAuthentication(InternalAuthorizationService.VANILLA));

		txService.invokeInTx(() -> {
			loadMetaPreProcess(widgetDtos, viewDtos, screenDtos);
			metaRepository.deleteAllMeta();
			bcUtil.process(bcDtos);
			screenAndNavigationGroupAndNavigationViewUtil.process(screenDtos);
			responsibilitiesProcess(screenDtos, viewDtos);
			loadMetaAfterProcess();
			return null;
		});
	}

	//TODO>>Draft. Refactor
	private void responsibilitiesProcess(List<ScreenSourceDto> screenDtos, List<ViewSourceDTO> viewDtos) {
		if (config.isViewAllowedRolesEnabled()) {
			Map<String, String> viewToScreenMap = metaRepository.getNavigationViews()
					.stream()
					.collect(Collectors.toMap(NavigationView::getViewName, NavigationView::getScreenName));

			List<Responsibilities> responsibilities = new ArrayList<>();
			long defaultDepartmentId = 0L; //TODO>>replace magic number with value from config
			viewDtos.forEach(view -> {
				view.getRolesAllowed().forEach(role -> {
					responsibilities.add(new Responsibilities()
							.setResponsibilityType(ResponsibilityType.VIEW)
							.setInternalRoleCD(new LOV(role))
							.setView(view.getName())
							.setDepartmentId(defaultDepartmentId));
				});
			});

			Map<String, ScreenSourceDto> screenNameToScreen = screenDtos.stream()
					.collect(Collectors.toMap(ScreenSourceDto::getName, sd -> sd));

			Map<String, Set<ScreenSourceDto>> rolesToScreens = new HashMap<>();
			viewDtos.forEach(v -> {
				if (viewToScreenMap.containsKey(v.getName())) {
					String screenName = viewToScreenMap.get(v.getName());
					v.getRolesAllowed().forEach(role -> {
						if (!rolesToScreens.containsKey(role)) {
							rolesToScreens.put(role, new HashSet<>());
						}
						rolesToScreens.get(role).add(screenNameToScreen.get(screenName));
					});
				}
			});

			for (Entry<String, Set<ScreenSourceDto>> entry : rolesToScreens.entrySet()) {
				String role = entry.getKey();
				Set<ScreenSourceDto> screens = entry.getValue();
				responsibilities.add(new Responsibilities()
						.setResponsibilityType(ResponsibilityType.SCREEN)
						.setInternalRoleCD(new LOV(role))
						.setScreens(mapToScreens(screenNameToScreen, screens))
						.setDepartmentId(defaultDepartmentId));
			}
			metaRepository.deleteAndSaveResponsibilities(responsibilities);

		}
	}


	//TODO>>Draft. Refactor
	@NonNull
	private String mapToScreens(@NonNull Map<String, ScreenSourceDto> screenNameToScreen,
			@NonNull Set<ScreenSourceDto> screens) {
		StringJoiner joiner = new StringJoiner(",");
		List<ScreenSourceDto> orderedScreens = screens
				.stream()
				.sorted(Comparator.comparing(ScreenSourceDto::getOrder).thenComparing(ScreenSourceDto::getName))
				.collect(Collectors.toList());
		for (int i = 0; i < orderedScreens.size(); i++) {
			ScreenSourceDto screen = orderedScreens.get(i);
			String s = "  {\n"
					+ "    \"id\": \"id" + i + "\",\n"
					+ "    \"name\": \"" + screen.getName() + "\",\n"
					+ "    \"text\": \"" + screen.getTitle() + "\",\n"
					+ "    \"url\": \"/screen/" + screen.getName() + "\",\n"
					+ "    \"icon\": \"" + screen.getIcon() + "\"\n"
					+ "  }";
			joiner.add(s);
		}
		String collect = joiner.toString();
		return "[\n" + collect + "\n]";
	}

	protected void loadMetaPreProcess(List<WidgetSourceDTO> widgetDtos,
			List<ViewSourceDTO> viewDtos,
			List<ScreenSourceDto> screenDtos) {

	}

	protected void loadMetaAfterProcess() {

	}

}
