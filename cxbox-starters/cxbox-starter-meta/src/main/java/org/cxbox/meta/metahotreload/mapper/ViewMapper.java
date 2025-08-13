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

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.data.ViewWidgetGroupDTO;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.metahotreload.util.JsonUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewMapper {

	private final WidgetMapper widgetMapper;

	private final AtomicLong viewSeq = new AtomicLong(0L);

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper objectMapper;

	public ViewDTO map(ViewSourceDTO dto, Map<String, WidgetSourceDTO> widgetNameToWidget) {
		AtomicInteger viewWidgetSeq = new AtomicInteger(0);
		ViewDTO viewDto = new ViewDTO()
				.setName(dto.getName())
				.setTemplate(dto.getTemplate())
				.setTitle(dto.getTitle())
				.setUrl(dto.getUrl())
				.setCustomizable(ofNullable(dto.getCustomizable()).orElse(false))
				.setEditable(ofNullable(dto.getEditable()).orElse(false))
				.setIgnoreHistory(ofNullable(dto.getIgnoreHistory()).orElse(false))
				.setOptions(JsonUtils.serializeOrElseEmptyArr(objectMapper, dto.getOptions()))
				.setReadOnly(false) //TODO>>take from responsibilities. temporary dropped in 4.0.0
				.setGroups(dto.getGroups().stream()
						.map(e -> new ViewWidgetGroupDTO()
								.setWidgetNames(e.getWidgetNames())
								.setCollapsedCondition(JsonUtils.serializeOrElseEmptyArr(objectMapper, e.getCollapsedCondition())
						)
						)
						.collect(Collectors.toList()))
				.setWidgets(dto.getWidgets()
						.stream()
						.map(vw -> widgetMapper.map(widgetNameToWidget.get(vw.getWidgetName()), vw))
						.map(vw -> {
							vw.setWidgetId(viewWidgetSeq.getAndIncrement());
							vw.setId(vw.getId());
							return vw;
						})
						.collect(Collectors.toList()));
		viewDto.setId(viewSeq.getAndIncrement());
		return viewDto;
	}


}
