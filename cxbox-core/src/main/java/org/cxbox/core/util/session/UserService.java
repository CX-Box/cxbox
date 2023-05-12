
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

import org.cxbox.api.data.PageSpecification;
import org.cxbox.api.data.ResultPage;
import org.cxbox.core.controller.UserController.UserDto;
import org.cxbox.model.core.entity.User;


public interface UserService {

	/**
	 * Get user mentioned by name or login
	 *
	 * @param mention search
	 * @param page request parameters
	 * @return ResultPage
	 */
	ResultPage<UserDto> getByMention(String mention, PageSpecification page);

	/**
	 * Get user by login
	 *
	 * @param login login
	 * @return User
	 */
	User getUserByLogin(String login);

}
