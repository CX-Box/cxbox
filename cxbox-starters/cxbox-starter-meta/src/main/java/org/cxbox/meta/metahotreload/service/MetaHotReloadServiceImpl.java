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
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.entity.Responsibilities;
import org.cxbox.meta.entity.Responsibilities.ResponsibilityType;
import org.cxbox.model.core.entity.BaseEntity;

@RequiredArgsConstructor
public class MetaHotReloadServiceImpl implements MetaHotReloadService {

	protected final MetaConfigurationProperties config;

	protected final MetaResourceReaderService metaResourceReaderService;

	protected final InternalAuthorizationService authzService;

	protected final TransactionService txService;

	protected final MetaRepository metaRepository;

	public void loadMeta() {
		List<ScreenSourceDto> screenDtos = metaResourceReaderService.getScreens();
		List<ViewSourceDTO> viewDtos = metaResourceReaderService.getViews();

		authzService.loginAs(authzService.createAuthentication(InternalAuthorizationService.VANILLA));

		txService.invokeInTx(() -> {
			metaRepository.deleteAllMeta();
			responsibilitiesProcess(screenDtos, viewDtos);
			loadMetaAfterProcess();
			return null;
		});
	}

	//TODO>>Draft. Refactor
	public void responsibilitiesProcess(List<ScreenSourceDto> screenDtos, List<ViewSourceDTO> viewDtos) {
		if (config.isViewAllowedRolesEnabled()) {

			Map<String, String> viewToScreenMap = new HashMap<>();
			List<Long> idCustomRecords = new ArrayList<>();
			List<Responsibilities> responsibilities = new ArrayList<>();
			long defaultDepartmentId = 0L; //TODO>>replace magic number with value from config

			metaRepository.getAllScreens()
					.forEach((screenName, screenDto) -> screenDto.getViews().stream().map(ViewDTO::getName)
							.forEach(viewName -> viewToScreenMap.put(viewName, screenName)));
			viewDtos.forEach(view -> {
				view.getRolesAllowed().forEach(role -> {
					responsibilities.add(new Responsibilities()
							.setResponsibilityType(ResponsibilityType.VIEW)
							.setInternalRoleCD(new LOV(role))
							.setView(view.getName())
							.setDepartmentId(defaultDepartmentId));
				});
			});

			List<Responsibilities> viewDB = metaRepository.getAllView();
			//block start - user records in table responsibilities
			if (!viewDB.isEmpty()) {
				//no start application with database changeset
				Map<String, Responsibilities> keyViewsBD = viewDB.stream()
						.collect(Collectors.toMap(
								a -> a.getInternalRoleCD().getKey() + a.getView(),
								resp -> resp
						));

				Map<String, Responsibilities> keyViewsMeta = responsibilities.stream()
						.collect(Collectors.toMap(
								a -> a.getInternalRoleCD().getKey() + a.getView(),
								resp -> resp
						));

				//Delete user records in meta
				List<Responsibilities> diffMetaDB = searchDiffData(keyViewsMeta, keyViewsBD);
				if (!diffMetaDB.isEmpty()) {
					responsibilities.removeAll(diffMetaDB);
				}

				//Ids - Not delete user records in database
				List<Responsibilities> diffDBMeta = searchDiffData(keyViewsBD, keyViewsMeta);
				if (!diffDBMeta.isEmpty()) {
					idCustomRecords = diffDBMeta.stream().map(BaseEntity::getId).toList();
				}
			}
			//block end

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
						.setScreens(mapToScreens(screens))
						.setDepartmentId(defaultDepartmentId));
			}
			metaRepository.deleteAndSaveResponsibilities(responsibilities, idCustomRecords);

		}
	}


	//TODO>>Draft. Refactor
	@NonNull
	public String mapToScreens(@NonNull Set<ScreenSourceDto> screens) {
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


	protected void loadMetaAfterProcess() {

	}

	private List<Responsibilities> searchDiffData(Map<String, Responsibilities> map,
			Map<String, Responsibilities> mapForSearch) {
		return map.entrySet().stream()
				.filter(keyView -> !mapForSearch.containsKey(keyView.getKey()))
				.map(Entry::getValue).toList();
	}

}
