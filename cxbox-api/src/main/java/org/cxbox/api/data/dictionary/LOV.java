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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.ToString;

/**
 * @deprecated
 * LOV is deprecated. Instead, use type safe <code>{@link org.cxbox.dictionary.Dictionary}</code>
 */
@Deprecated(since = "4.0.0-M12", forRemoval = true)
@Getter
@ToString(of = {"key"})
public final class LOV implements Serializable {

	private final String key;

	@JsonIgnore
	private final int hash;

	public LOV(@JsonProperty("key") String key) {
		this.key = key;
		this.hash = key != null ? key.hashCode() : 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof LOV)) {
			return false;
		}
		LOV lov = (LOV) o;
		return key == null ? lov.key == null : key.equals(lov.key);
	}

	@Override
	public int hashCode() {
		return hash;
	}

}
