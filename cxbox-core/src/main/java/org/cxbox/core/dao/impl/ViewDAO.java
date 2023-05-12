
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

package org.cxbox.core.dao.impl;

import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.ui.entity.View;
import org.cxbox.model.ui.entity.View_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ViewDAO {

	@Autowired
	private JpaDao jpaDao;

	public View findByName(String name) {
		return jpaDao.getSingleResultOrNull(View.class, (root, cq, cb) -> cb.equal(root.get(View_.name), name));
	}

}
