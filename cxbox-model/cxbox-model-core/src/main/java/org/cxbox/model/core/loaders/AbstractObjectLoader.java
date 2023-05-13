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

package org.cxbox.model.core.loaders;

import org.cxbox.api.service.ObjectLoader;
import org.hibernate.Hibernate;


public abstract class AbstractObjectLoader<T> implements ObjectLoader<T> {

	@Override
	public boolean accept(Object object) {
		return getType().isInstance(object);
	}

	protected abstract Class<? extends T> getType();

	protected <E> E load(E object) {
		Hibernate.initialize(object);
		return object;
	}

}
