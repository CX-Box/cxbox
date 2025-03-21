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

package org.cxbox.api.service.session;

import lombok.NonNull;
import org.cxbox.api.data.dictionary.LOV;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

public interface CxboxUserDetailsInterface extends UserDetails, IUser<Long> {

	Long getId();

	Long getDepartmentId();

	String getUsername();

	String getPassword();

	@NonNull
	Set<String> getUserRoles();

	LOV getTimezone();

	LOV getLocaleCd();

	Set<GrantedAuthority> getAuthorities();

	CxboxUserDetailsInterface setId(Long id);

	CxboxUserDetailsInterface setUsername(String username);

	CxboxUserDetailsInterface setPassword(String password);

	CxboxUserDetailsInterface setUserRoles(Set<String> userRole);

	CxboxUserDetailsInterface setTimezone(LOV timezone);

	CxboxUserDetailsInterface setLocaleCd(LOV localeCd);

	CxboxUserDetailsInterface setAuthorities(Set<GrantedAuthority> authorities);
}
