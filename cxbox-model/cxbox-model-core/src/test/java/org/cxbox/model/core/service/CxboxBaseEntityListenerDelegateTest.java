
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

package org.cxbox.model.core.service;

import static org.mockito.Mockito.when;

import org.cxbox.model.core.api.CurrentUserAware;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.model.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CxboxBaseEntityListenerDelegateTest {

	@Mock
	CurrentUserAware<User> currentUserAware;

	@InjectMocks
	CxboxBaseEntityListenerDelegate cxboxBaseEntityListenerDelegate;

	@Mock
	BaseEntity baseEntity;

	@Mock
	User user;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testBaseEntityOnLoad() {
		cxboxBaseEntityListenerDelegate.baseEntityOnLoad(baseEntity);
	}

	@Test
	void testBaseEntityOnCreate() {
		when(currentUserAware.getCurrentUser()).thenReturn(user);

		cxboxBaseEntityListenerDelegate.baseEntityOnCreate(baseEntity);
	}

	@Test
	void testBaseEntityOnUpdate() {
		when(currentUserAware.getCurrentUser()).thenReturn(user);

		cxboxBaseEntityListenerDelegate.baseEntityOnUpdate(baseEntity);
	}

}
