
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

package org.cxbox.crudma.impl;

import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.data.view.WidgetDTO;
import org.cxbox.core.dto.data.view.WidgetDTO_;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.crudma.api.WidgetService;
import org.cxbox.crudma.meta.WidgetFieldMetaBuilder;
import org.cxbox.model.ui.entity.Widget;
import org.springframework.stereotype.Service;


@Service
public class WidgetServiceImpl extends VersionAwareResponseService<WidgetDTO, Widget> implements WidgetService {

	public WidgetServiceImpl() {
		super(WidgetDTO.class, Widget.class, null, WidgetFieldMetaBuilder.class);
	}

	@Override
	protected CreateResult<WidgetDTO> doCreateEntity(final Widget entity, final BusinessComponent bc) {
		Long id = baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, baseDAO.findById(Widget.class, id)));
	}

	@Override
	protected ActionResultDTO<WidgetDTO> doUpdateEntity(Widget widget, WidgetDTO data, BusinessComponent bc) {
		if (data.hasChangedFields()) {
			if (data.isFieldChanged(WidgetDTO_.name)) {
				widget.setName(data.getName());
			}
			if (data.isFieldChanged(WidgetDTO_.bcName)) {
				widget.setBc(data.getBcName());
			}
			if (data.isFieldChanged(WidgetDTO_.showCondition)) {
				widget.setShowCondition(data.getShowCondition());
			}
			if (data.isFieldChanged(WidgetDTO_.fields)) {
				widget.setFields(data.getFields());
			}
			if (data.isFieldChanged(WidgetDTO_.axisFields)) {
				widget.setAxisFields(data.getAxisFields());
			}
			if (data.isFieldChanged(WidgetDTO_.chart)) {
				widget.setChart(data.getChart());
			}
			if (data.isFieldChanged(WidgetDTO_.options)) {
				widget.setOptions(data.getOptions());
			}
			if (data.isFieldChanged(WidgetDTO_.graph)) {
				widget.setGraph(data.getGraph());
			}
			if (data.isFieldChanged(WidgetDTO_.pivotFields)) {
				widget.setPivotFields(data.getPivotFields());
			}
			if (data.isFieldChanged(WidgetDTO_.type)) {
				widget.setType(data.getType());
			}
			if (data.isFieldChanged(WidgetDTO_.title)) {
				widget.setTitle(data.getTitle());
			}
		}
		return new ActionResultDTO<>(entityToDto(bc, widget));
	}

}
