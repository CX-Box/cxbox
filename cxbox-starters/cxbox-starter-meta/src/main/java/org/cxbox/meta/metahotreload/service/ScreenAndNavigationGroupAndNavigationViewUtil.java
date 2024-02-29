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

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.data.dictionary.CoreDictionaries;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.metahotreload.util.JsonUtils;
import org.cxbox.meta.entity.Screen;
import org.cxbox.meta.navigation.NavigationGroup;
import org.cxbox.meta.navigation.NavigationView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScreenAndNavigationGroupAndNavigationViewUtil {

	private final MetaRepository metaRepository;

	@Qualifier("cxboxObjectMapper")
	private final ObjectMapper objMapper;

	public void process(
			@NonNull List<ScreenSourceDto> screenDtos) {
		screenDtos.forEach(scr -> {
			Screen screen = mapToScreen(objMapper, scr);
			metaRepository.saveScreen(screen);
			if (scr.getNavigation() != null && scr.getNavigation().getMenu() != null) {
				AtomicInteger seq = new AtomicInteger(0);
				scr.getNavigation().getMenu()
						.forEach(menu -> dfs(scr.getName(), seq, null, menu, metaRepository));
			}
		});
	}



	@NonNull
	private static Screen mapToScreen(
			@NonNull ObjectMapper objectMapper,
			@NonNull ScreenSourceDto screenSourceDto) {
		return new Screen()
				.setName(screenSourceDto.getName())
				.setTitle(screenSourceDto.getTitle())
				.setPrimary(screenSourceDto.getPrimaryViewName())
				.setPrimaries(JsonUtils.serializeOrElseNull(objectMapper, screenSourceDto.getPrimaryViews()));
	}

	private static void dfs(@NonNull String screenName,
													@NonNull AtomicInteger seq,
			@Nullable NavigationGroup parent,
													@NonNull ScreenSourceDto.ScreenNavigationSourceDto.MenuItemSourceDto menuDto,
													@NonNull MetaRepository metaRepository) {
		if (menuDto.getChild() != null && !menuDto.getChild().isEmpty()) {
			NavigationGroup navigationGroup = mapToNavigationGroup(screenName, seq.get(), parent, menuDto);
			metaRepository.saveNavigationGroup(navigationGroup);
			seq.incrementAndGet();
			menuDto.getChild().forEach(child -> dfs(screenName, seq, navigationGroup, child, metaRepository));
		} else {
			NavigationView navigationView = mapToNavigationView(screenName, seq.get(), parent, menuDto);
			metaRepository.saveNavigationView(navigationView);
			seq.incrementAndGet();
		}
	}

	@NonNull
	private static NavigationGroup mapToNavigationGroup(
			@NonNull String screenName,
			int seq,
			@Nullable NavigationGroup parentNavigationGroup,
			@NonNull ScreenSourceDto.ScreenNavigationSourceDto.MenuItemSourceDto menu) {
		return new NavigationGroup()
				.setId(UUID.randomUUID().toString().replace("-", ""))
				.setTypeCd(CoreDictionaries.ViewGroupType.NAVIGATION)
				.setScreenName(screenName)
				.setTitle(menu.getTitle())
				.setParent(parentNavigationGroup)
				.setSeq(seq)
				.setDescription(null)
				.setDefaultView(ofNullable(menu.getDefaultView()).orElse(menu.getViewName()))
				.setHidden(ofNullable(menu.getHidden()).orElse(false));
	}

	@NonNull
	private static NavigationView mapToNavigationView(
			@NonNull String screenName,
			int seq,
			@Nullable NavigationGroup parentGroup,
			@NonNull ScreenSourceDto.ScreenNavigationSourceDto.MenuItemSourceDto menuDto) {
		return new NavigationView()
				.setId(UUID.randomUUID().toString().replace("-", ""))
				.setViewName(menuDto.getViewName())
				.setDescription(null)
				.setViewName(ofNullable(menuDto.getViewName()).orElse(menuDto.getDefaultView()))
				.setHidden(ofNullable(menuDto.getHidden()).orElse(false))
				.setSeq(seq)
				.setTypeCd(CoreDictionaries.ViewGroupType.NAVIGATION)
				.setScreenName(screenName)
				.setParentGroup(parentGroup);
	}

}
