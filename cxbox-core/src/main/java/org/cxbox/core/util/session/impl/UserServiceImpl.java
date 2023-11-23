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

package org.cxbox.core.util.session.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.core.util.session.UserService;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.core.entity.User;
import org.cxbox.model.core.entity.User_;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final JpaDao jpaDao;

	@Override
	public Long getUserByLogin(String login) {
		User user = jpaDao.getFirstResultOrNull(User.class, (root, cq, cb) ->
				cb.equal(
						cb.upper(root.get(User_.login)),
						login.toUpperCase()
				)
		);
		return Optional.ofNullable(user).map(BaseEntity::getId).orElse(null);
	}



}
