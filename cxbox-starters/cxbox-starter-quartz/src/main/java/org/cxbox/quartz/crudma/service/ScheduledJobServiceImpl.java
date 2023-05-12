
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

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;
import static org.cxbox.api.util.i18n.LocalizationFormatter.uiMessage;

import org.cxbox.api.data.dictionary.DictionaryType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.VersionAwareResponseService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.CreateResult;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.service.action.Actions;
import org.cxbox.quartz.crudma.dto.ScheduledJobDTO;
import org.cxbox.quartz.crudma.dto.ScheduledJobDTO_;
import org.cxbox.quartz.crudma.meta.ScheduledJobFieldMetaBuilder;
import org.cxbox.quartz.impl.SchedulerService;
import org.cxbox.quartz.model.ScheduledJob;
import org.cxbox.quartz.model.ScheduledJob_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
public class ScheduledJobServiceImpl extends VersionAwareResponseService<ScheduledJobDTO, ScheduledJob> implements
		ScheduledJobService {

	@Autowired
	private SchedulerService schedulerService;

	public ScheduledJobServiceImpl() {
		super(ScheduledJobDTO.class, ScheduledJob.class, null, ScheduledJobFieldMetaBuilder.class);
	}

	@Override
	protected Specification<ScheduledJob> getParentSpecification(BusinessComponent bc) {
		return (root, cq, cb) -> cb.and(
				super.getParentSpecification(bc).toPredicate(root, cq, cb),
				// не показываем совсем уж системные задачи
				cb.equal(root.get(ScheduledJob_.system), false)
		);
	}

	@Override
	public Actions<ScheduledJobDTO> getActions() {
		return Actions.<ScheduledJobDTO>builder()
				.create().add()
				.save().add()
				.delete().add()
				.action("launchNow", uiMessage("action.launch"))
				.available(this::isServiceDefined).invoker(this::launchNow).add()
				.build();
	}

	@Override
	protected CreateResult<ScheduledJobDTO> doCreateEntity(final ScheduledJob entity, final BusinessComponent bc) {
		baseDAO.save(entity);
		return new CreateResult<>(entityToDto(bc, entity));
	}

	private boolean isServiceDefined(BusinessComponent bc) {
		if (bc.getId() == null) {
			return false;
		}
		return baseDAO.findById(ScheduledJob.class, bc.getIdAsLong()).getService() != null;
	}

	private ActionResultDTO<ScheduledJobDTO> launchNow(final BusinessComponent bc, final ScheduledJobDTO data) {
		ScheduledJob job = baseDAO.findById(ScheduledJob.class, bc.getIdAsLong());
		schedulerService.launchNow(job);
		return new ActionResultDTO<>(entityToDto(bc, job));
	}

	@Override
	protected ActionResultDTO<ScheduledJobDTO> doUpdateEntity(ScheduledJob job, ScheduledJobDTO data,
			BusinessComponent bc) {
		if (data.hasChangedFields()) {
			if (data.isFieldChanged(ScheduledJobDTO_.serviceName)) {
				onServiceChanged(job, validateServiceName(data.getServiceName()));
			}
			if (data.isFieldChanged(ScheduledJobDTO_.cronExpression)) {
				job.setCronExpression(validateCronExpression(data.getCronExpression()));
			}
			if (data.isFieldChanged(ScheduledJobDTO_.active)) {
				job.setActive(data.isActive());
			}
		}
		schedulerService.scheduleJob(job);
		return new ActionResultDTO<>(entityToDto(bc, job)).setAction(PostAction.refreshBc(bc));
	}

	protected void onServiceChanged(ScheduledJob job, LOV serviceName) {
		job.setService(serviceName);
		job.getParams().forEach(param -> baseDAO.delete(param));
	}

	private LOV validateServiceName(String title) {
		LOV service = DictionaryType.SCHEDULED_SERVICES.lookupName(title);
		if (service != null) {
			return service;
		}
		throw new BusinessException().addPopup(errorMessage("error.empty_service_name"));
	}

	private String validateCronExpression(String cronExpression) {
		try {
			return schedulerService.validateCronExpression(cronExpression);
		} catch (IllegalArgumentException ex) {
			throw new BusinessException().addPopup(errorMessage("error.wrong_cron_expression"));
		}
	}

	@Override
	public ActionResultDTO<ScheduledJobDTO> deleteEntity(BusinessComponent bc) {
		ScheduledJob job = baseDAO.findById(ScheduledJob.class, bc.getIdAsLong());
		schedulerService.removeJob(job);
		job.getParams().forEach(param -> baseDAO.delete(param));
		return super.deleteEntity(bc);
	}

}
