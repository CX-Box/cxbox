/*
 * © OOO "SI IKS LAB", 2022-2024
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

import java.util.List;
import java.util.Map;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.state.BcStateAware;
import org.cxbox.core.dao.AnySourceBaseDAO;
import org.cxbox.core.external.core.EntityFirstLevelCache;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAnySourceBaseDAO<E> implements AnySourceBaseDAO<E> {

	@Autowired
	private EntityFirstLevelCache<E> cache;

	@Autowired
	private BcStateAware anySourceBcStateAware;

	@Override
	public String generateId() {
		return "-1";
	}

	/**
	 * Корректная реализация по умолчанию, но не эффективная по производительности.
	 * Рекомендуется использовать свою реализацию
	 */
	@Override
	public long count(BusinessComponent bc) {
		final Map<String, String> parameters = bc.getParameters().getParameters();
		parameters.put("_limit", "1");
		parameters.put("_page", "1");
		final BusinessComponent countBc = new BusinessComponent(bc.getId(), bc.getParentId(), bc.getDescription(), bc.getHierarchy(), new QueryParameters(parameters));
		return getList(countBc, countBc.getParameters()).getTotalElements();
	}

	@Override
	public E getById(BusinessComponent bc) {
		return cache.getCache().computeIfAbsent(bc.getName(), bcName -> getByIdIgnoringFirstLevelCache(bc));
	}

	@Override
	public E setWithFirstLevelCache(BusinessComponent bc, E entity) {
		return cache.getCache().put(bc.getName(), entity);
	}

	@Override
	public E flush(BusinessComponent bc) {
		if (anySourceBcStateAware.isPersisted(bc)) {
			return setWithFirstLevelCache(bc, update(bc, getById(bc)));
		} else {
			return setWithFirstLevelCache(bc, create(bc, getById(bc)));
		}
	}

	@Override
	public void associate(BusinessComponent bc, String parentId, List<String> childs) {
		throw new UnsupportedOperationException();
	}

}
