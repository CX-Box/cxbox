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

package org.cxbox.dictionary;

import java.io.Serializable;
import java.util.Collection;
import lombok.NonNull;

/**
 * see description in <code>{@link org.cxbox.dictionary.Dictionary}</code>
 */
public interface DictionaryProvider {

	<T extends Dictionary> T lookupName(@NonNull Class<T> type, @NonNull DictionaryValue value);

	<T extends Dictionary> DictionaryValue lookupValue(@NonNull T dictionary);

	@NonNull
	<T extends Dictionary> Collection<T> getAll(@NonNull Class<T> type);

	interface DictionaryValue extends Serializable {

		@NonNull
		String getValue();

	}

}
