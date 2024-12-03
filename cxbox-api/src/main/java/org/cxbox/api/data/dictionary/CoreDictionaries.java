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

import lombok.experimental.UtilityClass;


/**
 * @deprecated LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
 */
@Deprecated(since = "4.0.0-M12", forRemoval = true)
public class CoreDictionaries {

	@UtilityClass
	public static final class PreInvokeType {

		public static final LOV CONFIRMATION = new LOV("confirm");

		public static final LOV INFORMATION = new LOV("info");

		public static final LOV ERROR = new LOV("error");

	}

	@UtilityClass
	public static class InternalRole {

		public static final String ADMIN = "ADMIN";

	}

	@UtilityClass
	public static final class SystemPref {

		public static final LOV SUPPORTED_LANGUAGES = new LOV("SUPPORTED_LANGUAGES");

		public static final LOV SYSTEM_URL = new LOV("SYSTEM_URL");

		public static final LOV FEATURE_EXCEPTION_TRACKING = new LOV("FEATURE_EXCEPTION_TRACKING");

		public static final LOV FEATURE_FULL_STACKTRACES = new LOV("FEATURE_FULL_STACKTRACES");

	}

	@UtilityClass
	public static final class DictionaryTermType {

		public static final LOV DEPT = new LOV("DEPT");

		public static final LOV TEXT_FIELD = new LOV("TEXT_FIELD");

		public static final LOV DICTIONARY_FIELD = new LOV("DICTIONARY_FIELD");

		public static final LOV BC = new LOV("BC");

		public static final LOV FIELD_IS_EMPTY = new LOV("FIELD_IS_EMPTY");

		public static final LOV BOOLEAN_FIELD = new LOV("BOOLEAN_FIELD");

	}

}
