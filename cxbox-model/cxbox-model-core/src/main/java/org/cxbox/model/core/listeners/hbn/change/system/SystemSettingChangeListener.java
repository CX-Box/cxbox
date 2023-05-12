
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

package org.cxbox.model.core.listeners.hbn.change.system;

import org.cxbox.api.data.dao.databaselistener.IChangeListener;
import org.cxbox.api.data.dao.databaselistener.IChangeVector;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.system.SystemSettings;
import org.cxbox.api.util.Invoker;
import org.cxbox.model.core.entity.SystemSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class SystemSettingChangeListener implements IChangeListener<SystemSetting> {

	private final SystemSettings systemSettings;

	private final TransactionService txService;

	@Override
	public Class<? extends SystemSetting> getType() {
		return SystemSetting.class;
	}

	@Override
	public void process(IChangeVector vector, LOV event) {
		txService.invokeAfterCompletion(Invoker.of(systemSettings::reload));
	}

}
