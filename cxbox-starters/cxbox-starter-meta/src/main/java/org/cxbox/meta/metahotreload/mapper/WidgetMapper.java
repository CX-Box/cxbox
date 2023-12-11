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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.meta.data.WidgetDTO;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO.ViewWidgetSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.metahotreload.util.JsonUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WidgetMapper {

	private final AtomicInteger widgetSeq = new AtomicInteger(0);

	@Qualifier("cxboxObjectMapper")
	private final ObjectMapper objectMapper;

	private final BcRegistry bcRegistry;

	public WidgetDTO map(WidgetSourceDTO dto, ViewWidgetSourceDTO viewWidget) {
		WidgetDTO widgetDTO = new WidgetDTO()
				.setName(dto.getName())
				.setType(dto.getType())
				.setUrl(bcRegistry.getUrlFromBc(dto.getBc()))
				.setBcName(dto.getBc())
				.setTitle(dto.getTitle())
				.setFields(JsonUtils.serializeOrElseEmptyArr(objectMapper, dto.getFields()))
				.setOptions(JsonUtils.serializeOrElseEmptyArr(objectMapper, dto.getOptions()))
				.setPivotFields(JsonUtils.serializeOrElseNull(objectMapper, dto.getPivotFields()))
				.setAxisFields(JsonUtils.serializeOrElseEmptyArr(objectMapper, dto.getAxisFields()))
				.setShowCondition(JsonUtils.serializeOrElseEmptyArr(objectMapper, dto.getShowCondition()))
				.setChart(JsonUtils.serializeOrElseEmptyArr(objectMapper, dto.getChart()))
				.setGraph(JsonUtils.serializeOrElseNull(objectMapper, dto.getGraph()));
		widgetDTO.setWidgetId(widgetSeq.getAndIncrement());



		widgetDTO
				.setPosition(viewWidget.getPosition())
				.setDescriptionTitle(viewWidget.getDescriptionTitle())
				.setDescription(null) //support removed in 4.0.0
				.setSnippet(viewWidget.getSnippet())
				.setLimit(Optional.ofNullable(viewWidget.getPageLimit()).orElse(0L)) //TODO>>4.0.0>>why 0 by default?
				.setGridWidth(ofNullable(viewWidget.getGridWidth()).orElse(1L))
				.setGridBreak(ofNullable(viewWidget.getGridBreak()).orElse(0L))
				.setHide(ofNullable(viewWidget.getHideByDefault()).orElse(false))
				.setShowExportStamp(ofNullable(viewWidget.getShowExportStamp()).orElse(false));
		return widgetDTO;
	}
}
