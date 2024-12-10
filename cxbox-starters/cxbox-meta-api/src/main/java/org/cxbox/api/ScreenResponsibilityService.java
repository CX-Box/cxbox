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

package org.cxbox.api;

import java.util.Set;
import org.cxbox.dto.ScreenResponsibility;
import org.cxbox.api.service.session.IUser;

import java.util.List;

/**
 * Service MUST read meta-data from json files (*.widget.json, *.screen.json, *.view.json and so on) and map it to DTO's that front-end waits during login. Front end will UI according to this dto. Optionally service can store intermediate results in persistent storages/caches
 */
public interface ScreenResponsibilityService {

	List<ScreenResponsibility> getScreens(IUser<Long> user, Set<String> userRole);

}
