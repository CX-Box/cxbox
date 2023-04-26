/*-
 * #%L
 * IO Cxbox - Testing
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.testing.tests;

import org.cxbox.core.bc.InnerBcTypeAware;
import org.cxbox.core.config.JacksonConfig;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.BcRegistryImpl;
import org.cxbox.core.dao.impl.SearchSpecDao;
import org.cxbox.core.service.impl.ValidatorsProviderImpl;
import org.cxbox.core.dto.mapper.DtoConstructorService;
import org.cxbox.core.dto.mapper.RequestValueCache;
import org.cxbox.core.service.DTOMapper;
import org.cxbox.core.service.ResponseFactory;
import org.cxbox.core.service.ResponseService;
import org.cxbox.core.ui.BcUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@ContextHierarchy(
		@ContextConfiguration(
				name = "child",
				classes = {
						DtoConstructorService.class,
						DTOMapper.class,
						RequestValueCache.class,
						BcUtils.class,
						InnerBcTypeAware.class,
						ResponseFactory.class,
						BcRegistryImpl.class,
						SearchSpecDao.class,
						ValidatorsProviderImpl.class,
						JacksonConfig.class
				}
		)
)
public abstract class AbstractResponseServiceTest<T extends ResponseService> extends BaseDAOAwareTest {

	protected abstract Class<T> getServiceClass();

	protected T getService() {
		return applicationContext.getBean(getServiceClass());
	}

	protected BusinessComponent createBc(BcDescription bc) {
		return createBc(bc, null);
	}

	protected BusinessComponent createBc(BcDescription bc, String id) {
		return createBc(bc, id, null);
	}

	protected BusinessComponent createBc(BcDescription bc, String id, String parentId) {
		return new BusinessComponent(id, parentId, bc);
	}

}

