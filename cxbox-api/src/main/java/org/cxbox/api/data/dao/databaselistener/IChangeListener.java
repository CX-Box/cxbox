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

package org.cxbox.api.data.dao.databaselistener;

import org.cxbox.api.data.dictionary.LOV;
import javax.persistence.metamodel.Attribute;
import org.springframework.core.Ordered;


public interface IChangeListener<E> extends Ordered {

	Class<? extends E> getType();

	default boolean isSupported(Object entity) {
		return getType().isInstance(entity);
	}

	default boolean canProcess(IChangeVector vector, LOV event) {
		return isSupported(vector.getEntity());
	}

	void process(IChangeVector vector, LOV event);

	@Override
	default int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	default <V> boolean isFieldChanged(IChangeVector vector, Attribute<E, V> field) {
		if (vector.isNew()) {
			return vector.getValue(field) != null;
		} else if (vector.isUpdate()) {
			return vector.hasChanged(field);
		}
		return false;
	}

}
