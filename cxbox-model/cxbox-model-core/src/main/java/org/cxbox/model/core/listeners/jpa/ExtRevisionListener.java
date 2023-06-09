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

package org.cxbox.model.core.listeners.jpa;

import org.cxbox.model.core.api.CurrentUserAware;
import org.cxbox.model.core.entity.ExtRevisionEntity;
import org.cxbox.model.core.entity.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.RevisionListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ExtRevisionListener implements RevisionListener {

	private final CurrentUserAware<User> currentUserAware;

	@Override
	public void newRevision(final Object revisionEntity) {
		final ExtRevisionEntity extRevisionEntity = (ExtRevisionEntity) revisionEntity;
		User currentUser = currentUserAware.getCurrentUser();
		extRevisionEntity.setUser(currentUser);
	}

}
