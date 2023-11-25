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

import java.util.Map;
import java.util.Set;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.session.IUser;

/**
 * Service that defines access rights to screens and views
 */
public interface ResponsibilitiesService {

	Map<String, Boolean> getListRespByUser(IUser<Long> user, LOV userRole);

	String getListScreensByUser(IUser<Long> user, LOV userRole);

	@Deprecated
	Set<String> getViewResponsibilities(Long departmentId);

}
