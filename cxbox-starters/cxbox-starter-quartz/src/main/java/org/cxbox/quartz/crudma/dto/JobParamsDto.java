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

package org.cxbox.quartz.crudma.dto;

import static org.cxbox.api.data.dictionary.DictionaryType.JOB_PARAM;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.core.dto.Lov;
import org.cxbox.quartz.model.JobParams;
import org.cxbox.quartz.model.ScheduledJob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobParamsDto extends DataResponseDTO {

	private ScheduledJob scheduledJob;

	@Lov(JOB_PARAM)
	private String jobParamCd;

	private Long paramValueNumber;

	public JobParamsDto(JobParams jobParams) {
		this.jobParamCd = JOB_PARAM.lookupValue(jobParams.getJobParamCd());
		this.scheduledJob = jobParams.getScheduledJob();
		this.paramValueNumber = jobParams.getParamValueNumber();
	}

}
