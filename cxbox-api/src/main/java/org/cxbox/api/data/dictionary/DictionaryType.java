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

package org.cxbox.api.data.dictionary;

import static org.cxbox.api.data.dictionary.DictionaryCache.dictionary;

import java.io.Serializable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @deprecated
 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
 */
@Deprecated(since = "4.0.0-M12", forRemoval = true)
@Getter
@RequiredArgsConstructor
public enum DictionaryType implements Serializable, IDictionaryType {

	NOTIFICATION_SETTINGS_TYPE,
	DATABASE_EVENT,
	NOTIFICATION_RECIPIENT_TYPE,
	NOTIFICATION_DELIVERY_TYPE,
	MIME_TYPE,
	TASK_CATEGORY,
	TASK_TYPE,
	TASK_STATUS,
	TIMEZONE,
	REPORT_PERIOD,
	TASK_PRIORITY,
	PERIODICAL_TYPE,
	DAY_TYPE,
	ACTIVITY_TYPE,
	SUPERVISORY_FACT_STATUS,
	TYPE_OBJECT,
	FILE_STORAGE,
	SCHEDULED_SERVICES,
	SCHEDULED_SERVICES_PARAMS,
	ES_INDEX_TYPES,
	JOB_PARAM,
	DICTIONARY_TERM_TYPE,
	BUSINESS_SERVICE_NAME,
	BUSINESS_SERVICE_URL,
	LAUNCH_STATUS;

	@Override
	public LOV lookupName(String val) {
		return dictionary().lookupName(val, this);
	}

	@Override
	public String lookupValue(LOV lov) {
		return dictionary().lookupValue(lov, this);
	}

	@Override
	public String getName() {
		return name();
	}


}
