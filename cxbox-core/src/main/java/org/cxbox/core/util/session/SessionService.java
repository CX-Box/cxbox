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

package org.cxbox.core.util.session;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.model.core.entity.Department;
import org.cxbox.model.core.entity.Division;
import org.cxbox.model.core.entity.User;
import org.cxbox.model.core.entity.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface SessionService {

	User getSessionUser();

	Department getSessionUserDepartment();

	LOV getSessionUserRole();

	void setSessionUserTimezone(LOV timezone);

	void setSessionUserLocale(LOV locale);

	void setSessionUserInternalRole(String role);

	String getSessionIpAddress();

	Map<String, Boolean> getResponsibilities();

	String getFirstViewFromResponsibilities(String... views);

	String getSessionId();

	Division getSessionUserDivision(LOV levelCd);

	Division getSessionUserDivision();

	List<UserRole> getSessionUserRoles();

	Collection<String> getCurrentScreenViews();

	List<String> getViews(String screenName);

}
