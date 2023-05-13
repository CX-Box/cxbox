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

package org.cxbox.core.service;

import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.bc.BusinessComponent;
import java.time.LocalDateTime;


public interface IOutwardReportEngineService {

	boolean isOutwardsReportAvailable(BusinessComponent bc);

	String getOutwardReportFormattedUrl(BusinessComponent bc, QueryParameters queryParams);

	String getOutwardReportFormattedUrlForTimePeriod(BusinessComponent bc, LocalDateTime startDate,
			LocalDateTime endDate);

	String getOutwardReportName(BusinessComponent bc);

}
