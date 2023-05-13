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

package org.cxbox.core.metahotreload.service;

import org.cxbox.core.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.core.metahotreload.dto.ScreenSourceDto;
import org.cxbox.core.metahotreload.dto.BcSourceDTO;
import org.cxbox.core.metahotreload.dto.ViewSourceDTO;
import org.cxbox.core.metahotreload.dto.WidgetSourceDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

@RequiredArgsConstructor
public class MetaResourceReaderService {

	final ApplicationContext applicationContext;

	final MetaConfigurationProperties config;

	@Qualifier("cxboxObjectMapper")
	final ObjectMapper objMapper;

	@NonNull
	public List<ScreenSourceDto> getScreens() {
		return readFilesToDto(ScreenSourceDto.class, config.getDirectory() + config.getScreenPath())
				.stream()
				.map(Pair::getLeft)
				.collect(Collectors.toList());
	}

	@NonNull
	public List<ViewSourceDTO> getViews() {
		return readFilesToDto(ViewSourceDTO.class, config.getDirectory() + config.getViewPath())
				.stream()
				.map(Pair::getLeft)
				.collect(Collectors.toList());
	}

	@NonNull
	public List<WidgetSourceDTO> getWidgets() {
		return readFilesToDto(WidgetSourceDTO.class, config.getDirectory() + config.getWidgetPath())
				.stream()
				.map(Pair::getLeft)
				.collect(Collectors.toList());
	}

	@NonNull
	public List<BcSourceDTO> getBcs() {
		final List<Pair<BcSourceDTO, Resource>> pairs = readFilesToDto(BcSourceDTO.class, config.getDirectory() + config.getBcPath());
		pairs.forEach(pair -> {
			final BcSourceDTO dto = pair.getLeft();
			final Resource resource = pair.getRight();
			final String query = readRelativeResource(resource, new File(dto.getQueryFile()).getName()).lines().collect(Collectors.joining("\n"));
			dto.setQuery(query);
		});
		return pairs
				.stream()
				.map(Pair::getLeft)
				.collect(Collectors.toList());
	}

	@NonNull
	@SneakyThrows
	private <T> List<Pair<T, Resource>> readFilesToDto(@NonNull Class<T> clazz, @NonNull String locationPattern) {
		return Arrays.stream(applicationContext.getResources(locationPattern))
				.map(resource -> readDto(resource, clazz))
				.collect(Collectors.toList());
	}

	@NonNull
	@SneakyThrows
	private <T> Pair<T, Resource> readDto(@NonNull Resource resource, @NonNull Class<T> valueType) {
		return Pair.of(objMapper.readValue(readResource(resource), valueType), resource);
	}

	@NonNull
	@SneakyThrows
	private BufferedReader readRelativeResource(@NonNull Resource resource, @NonNull String relativeFileName) {
		final Resource relative = resource.createRelative(relativeFileName);
		return readResource(relative);
	}

	@NonNull
	@SneakyThrows
	private BufferedReader readResource(@NonNull Resource resource) {
		return new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
	}


}
