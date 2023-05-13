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

package org.cxbox.core.service.impl;

import static org.cxbox.api.data.dictionary.CoreDictionaries.SystemPref.FEATURE_EXCEPTION_TRACKING;
import static org.cxbox.api.data.dictionary.CoreDictionaries.SystemPref.FEATURE_FULL_STACKTRACES;

import org.cxbox.api.system.ISystemSettingChangeEventListener;
import org.cxbox.api.system.SystemSettingChangedEvent;
import org.cxbox.api.system.SystemSettings;
import org.cxbox.core.exception.ExceptionHandlerSettings;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Getter
@Service
public class ExceptionHandlerSettingsImpl implements ExceptionHandlerSettings, ISystemSettingChangeEventListener {

	private boolean trackExceptions;

	private boolean fullStackTraces;

	@Autowired
	private SystemSettings systemSettings;

	@Override
	public void onApplicationEvent(SystemSettingChangedEvent event) {
		if (FEATURE_EXCEPTION_TRACKING.equals(event.getSetting())) {
			this.trackExceptions = systemSettings.getBooleanValue(FEATURE_EXCEPTION_TRACKING);
		}
		if (FEATURE_FULL_STACKTRACES.equals(event.getSetting())) {
			this.fullStackTraces = systemSettings.getBooleanValue(FEATURE_FULL_STACKTRACES);
		}
	}

	@PostConstruct
	protected void init() {
		this.trackExceptions = systemSettings.getBooleanValue(FEATURE_EXCEPTION_TRACKING);
		this.fullStackTraces = systemSettings.getBooleanValue(FEATURE_FULL_STACKTRACES);
	}

}
