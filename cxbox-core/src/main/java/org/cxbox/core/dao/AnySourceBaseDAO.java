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

package org.cxbox.core.dao;

import java.util.List;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.dao.impl.AbstractAnySourceBaseDAO;
import org.springframework.data.domain.Page;

public interface AnySourceBaseDAO<E> {

	String generateId();

	String getId(final E entity);

	void setId(final String id, final E entity);

	/**
	 * Should not be used anywhere except of {@link AbstractAnySourceBaseDAO}
	 * @param bc
	 * @return
	 */
	E getByIdIgnoringFirstLevelCache(BusinessComponent bc);

	/**
	 * Delete entity in anySource system
	 */
	void delete(BusinessComponent bc);

	/**
	 * @param queryParameters get filtering parameters from UI (not contains parent-child relationship or security specification)
	 */
	Page<E> getList(BusinessComponent bc, QueryParameters queryParameters);

	long count(BusinessComponent bc);

	/**
	 * Get from First Level Cache or from anySource system (if cache is empty)
	 */
	E getById(BusinessComponent bc);

	/**
	 * Put to First Level Cache
	 */
	void setWithFirstLevelCache(BusinessComponent bc, E entity);

	/**
	 * Should not be used anywhere except of {@link AbstractAnySourceBaseDAO}
	 * @param bc
	 * @return
	 */
	E update(BusinessComponent bc, E entity);

	/**
	 * Should be used to explicitly update/create in anySource system by custom action
	 * @param bc
	 * @return
	 */
	void flush(BusinessComponent bc);

	/**
	 * Should not be used anywhere except of {@link AbstractAnySourceBaseDAO}
	 * @param bc
	 * @return
	 */
	E create(BusinessComponent bc, E entity);

	void associate(BusinessComponent bc, String parentId, List<String> childs);

}
