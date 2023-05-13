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

package org.cxbox.model.dictionary.listener;

import org.cxbox.api.data.dao.databaselistener.IChangeListener;
import org.cxbox.api.data.dao.databaselistener.IChangeVector;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.dictionary.entity.AudDictionary;
import org.cxbox.model.dictionary.entity.DictionaryItem;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DictionaryItemChangeListener implements IChangeListener<DictionaryItem> {

	@Autowired
	private JpaDao jpaDao;

	@Override
	public Class<? extends DictionaryItem> getType() {
		return DictionaryItem.class;
	}

	@Override
	public void process(IChangeVector vector, LOV lov) {
		// todo: how to handle value changes?
		AudDictionary audDictionary = new AudDictionary();
		if (vector.getEventName() != null) {
			audDictionary.setEventType(vector.getEventName().getKey());
		}
		audDictionary.setEventDate(LocalDateTime.now());
		DictionaryItem item = vector.unwrap(getType());
		audDictionary.setDictType(item.getType());
		audDictionary.setKey(item.getKey());
		jpaDao.save(audDictionary);
	}

}
