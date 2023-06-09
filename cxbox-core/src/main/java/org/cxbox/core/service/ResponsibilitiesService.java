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

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.model.core.entity.Department;
import org.cxbox.model.core.entity.User;
import java.util.Map;
import java.util.Set;

/**
 * Service that defines access rights to screens and views
 */
public interface ResponsibilitiesService {

	Map<String, Boolean> getListRespByUser(User user, LOV userRole);

	String getListScreensByUser(User user, LOV userRole);

	@Deprecated
	Set<String> getViewResponsibilities(Department department);

}
