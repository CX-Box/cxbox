
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

package org.cxbox.quartz.crudma.meta;


import org.cxbox.api.data.dictionary.CoreDictionaries.LaunchStatus;
import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.api.data.dictionary.DictionaryType;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.rowmeta.FieldMetaBuilder;
import org.cxbox.model.core.dao.JpaDao;
import java.util.ArrayList;
import java.util.List;

import org.cxbox.quartz.crudma.dto.ScheduledJobDTO;
import org.cxbox.quartz.crudma.dto.ScheduledJobDTO_;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ScheduledJobFieldMetaBuilder extends FieldMetaBuilder<ScheduledJobDTO> {

	private final DictionaryCache dictionaryCache;

	@Autowired
	JpaDao jpaDao;

	@Override
	public void buildRowDependentMeta(RowDependentFieldsMeta<ScheduledJobDTO> fields, InnerBcDescription bcDescription,
			Long rowId, Long parRowId) {
		buildRowDependentCommonMeta(fields, bcDescription, parRowId);
	}

	private void buildRowDependentCommonMeta(RowDependentFieldsMeta<ScheduledJobDTO> fields,
			InnerBcDescription bcDescription, Long parRowId) {
		fields.setEnabled(ScheduledJobDTO_.serviceName, ScheduledJobDTO_.cronExpression, ScheduledJobDTO_.active);
		fields.setRequired(ScheduledJobDTO_.serviceName, ScheduledJobDTO_.cronExpression);
		fields.setDictionaryTypeWithAllValues(ScheduledJobDTO_.serviceName, DictionaryType.SCHEDULED_SERVICES);
	}

	@Override
	public void buildIndependentMeta(FieldsMeta<ScheduledJobDTO> fields, InnerBcDescription bcDescription,
			Long parRowId) {
		fields.enableFilter(ScheduledJobDTO_.serviceName, ScheduledJobDTO_.active, ScheduledJobDTO_.lastLaunchDate, ScheduledJobDTO_.launchStatusCd);
		fields.setAllFilterValuesByLovType(ScheduledJobDTO_.serviceName, DictionaryType.SCHEDULED_SERVICES);
		fields.setConcreteFilterValues(ScheduledJobDTO_.launchStatusCd, getApplicableStatuses());
	}

	private List<SimpleDictionary> getApplicableStatuses() {
		ArrayList<SimpleDictionary> dictDTOS = new ArrayList<>();
		dictDTOS.add(dictionaryCache.get(DictionaryType.LAUNCH_STATUS, LaunchStatus.SUCCESS.getKey()));
		dictDTOS.add(dictionaryCache.get(DictionaryType.LAUNCH_STATUS, LaunchStatus.FAILED.getKey()));
		return dictDTOS;
	}

}
