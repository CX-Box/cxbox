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

package org.cxbox.quartz.crudma.service;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.service.action.Actions;
import org.cxbox.quartz.crudma.dto.ScheduledJobParamDTO;
import org.cxbox.quartz.crudma.dto.ScheduledJobParamDTO_;
import org.cxbox.quartz.crudma.meta.ScheduledJobParamFieldMetaBuilder;
import org.cxbox.quartz.model.ScheduledJobParam;
import org.cxbox.quartz.model.ScheduledJobParam_;
import org.cxbox.quartz.model.ScheduledJob_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ScheduledJobParamServiceImpl extends
		VersionAwareResponseService<ScheduledJobParamDTO, ScheduledJobParam>
		implements ScheduledJobParamService {

	public ScheduledJobParamServiceImpl() {
		super(
				ScheduledJobParamDTO.class,
				ScheduledJobParam.class,
				ScheduledJobParam_.job,
				ScheduledJobParamFieldMetaBuilder.class
		);
	}

	@Override
	protected Specification<ScheduledJobParam> getParentSpecification(BusinessComponent bc) {
		return (root, query, cb) -> cb.and(
				cb.equal(root.get(ScheduledJobParam_.job).get(ScheduledJob_.service), getServiceName()),
				cb.isNotNull(root.get(ScheduledJobParam_.job).get(ScheduledJob_.service)),
				cb.equal(root.get(ScheduledJobParam_.job).get(ScheduledJob_.id), bc.getParentIdAsLong())
		);
	}

	protected LOV getServiceName() {
		return null;
	}

	@Override
	protected CreateResult<ScheduledJobParamDTO> doCreateEntity(final ScheduledJobParam entity,
			final BusinessComponent bc) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ActionResultDTO<ScheduledJobParamDTO> doUpdateEntity(ScheduledJobParam entity, ScheduledJobParamDTO data,
			BusinessComponent bc) {
		if (entity == null) {
			throw new IllegalArgumentException("Не удалось найти параметр с id: ".concat(bc.getId()));
		}
		if (data.isFieldChanged(ScheduledJobParamDTO_.paramName)) {
			onParameterNameChanged(entity, data.getParamName());
		}
		if (data.isFieldChanged(ScheduledJobParamDTO_.paramValue)) {
			onParameterValueChanged(entity, entity.getParamName(), data.getParamValue());
		}
		return new ActionResultDTO<>(entityToDto(bc, entity));
	}

	protected void onParameterNameChanged(ScheduledJobParam entity, String parameterName) {

	}

	protected void onParameterValueChanged(ScheduledJobParam entity, LOV parameterName, String value) {
		entity.setParamValue(value);
	}

	@Override
	public Actions<ScheduledJobParamDTO> getActions() {
		return Actions.<ScheduledJobParamDTO>builder()
				.save().add()
				.addAll(super.getActions())
				.build();
	}

}
