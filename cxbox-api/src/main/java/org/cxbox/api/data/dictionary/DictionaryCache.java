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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


public interface DictionaryCache {

	AtomicReference<DictionaryCache> instance = new AtomicReference<>();

	static DictionaryCache dictionary() {
		return instance.get();
	}

	void reload();

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	SimpleDictionary get(IDictionaryType type, String key);

	SimpleDictionary get(String type, String key);

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	Collection<SimpleDictionary> getAll(IDictionaryType dictionaryType);

	Collection<SimpleDictionary> getAll(String dictionaryType);

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	String lookupValue(LOV key, IDictionaryType type);

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	String lookupValue(LOV key, String type);

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	String lookupValueNullable(LOV key, IDictionaryType type);

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	LOV lookupName(String val, IDictionaryType type);

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	LOV lookupName(String val, String type);

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	boolean containsKey(String key, IDictionaryType type);

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	boolean containsKey(LOV key, IDictionaryType type);

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	boolean containsValue(String value, IDictionaryType type);

	/**
	 * @deprecated
	 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	String getDescription(String key, IDictionaryType type);

	Set<String> types();

}
