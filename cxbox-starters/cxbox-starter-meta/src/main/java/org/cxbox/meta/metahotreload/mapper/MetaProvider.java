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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cxbox.core.config.cache.CacheConfig;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.entity.BcProperties;
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.entity.FilterGroup;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.metahotreload.repository.MetaRepository;
import org.cxbox.meta.metahotreload.service.MetaResourceReaderService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetaProvider {

	private final MetaResourceReaderService metaResourceReaderService;

	private final MetaRepository metaRepository;

	private final ScreenMapper screenMapper;

	private final ViewMapper viewMapper;

	@Cacheable(cacheResolver = CacheConfig.CXBOX_CACHE_RESOLVER,
			cacheNames = CacheConfig.UI_CACHE,
			key = "{#root.methodName}"
	)
	public Map<String, ScreenDTO> getAllScreens() {
		//load data
		var screens = metaResourceReaderService.getScreens();
		var widgets = metaResourceReaderService.getWidgets();
		var views = metaResourceReaderService.getViews();
		Map<String, BcProperties> bcProps = metaRepository.getBcProperties();
		Map<String, List<FilterGroup>> filterGroups = metaRepository.getFilterGroups();

		//map data
		var widgetNameToWidget = widgets.stream()
				.collect(Collectors.toMap(WidgetSourceDTO::getName, e -> e));
		var viewNameToView = views
				.stream()
				.map(v -> viewMapper.map(v, widgetNameToWidget))
				.collect(Collectors.toMap(ViewDTO::getName, e -> e));
		return screens.stream()
				.map(screenSourceDto -> screenMapper.map(screenSourceDto, viewNameToView, bcProps, filterGroups))
				.collect(Collectors.toMap(ScreenDTO::getName, e -> e));
	}

}
