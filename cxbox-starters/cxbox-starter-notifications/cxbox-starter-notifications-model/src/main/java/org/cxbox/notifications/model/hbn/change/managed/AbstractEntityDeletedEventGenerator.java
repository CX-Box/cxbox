/*-
 * #%L
 * IO Cxbox - Model Core
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

package org.cxbox.notifications.model.hbn.change.managed;

import org.cxbox.api.data.dao.databaselistener.IChangeVector;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.notifications.model.api.INotificationEventBuilder;
import org.cxbox.model.core.entity.BaseEntity;
import org.cxbox.notifications.model.hbn.change.AbstractEventGenerator;


public abstract class AbstractEntityDeletedEventGenerator<E extends BaseEntity> extends AbstractEventGenerator<E> {

	@Override
	public void process(IChangeVector vector, LOV event) {
		E entity = vector.unwrap(getType());
		if (vector.isDelete()) {
			getBuilder(entity).publish();
		}
	}

	protected INotificationEventBuilder getBuilder(E entity) {
		return builder(entity, getEvent())
				.setPerformer(getPerformer());
	}

	protected abstract LOV getEvent();

	@Override
	public boolean canProcess(IChangeVector vector, LOV event) {
		if (!super.canProcess(vector, event)) {
			return false;
		}
		return vector.isDelete();
	}

	protected abstract INotificationEventBuilder builder(E entity, LOV event);

}
