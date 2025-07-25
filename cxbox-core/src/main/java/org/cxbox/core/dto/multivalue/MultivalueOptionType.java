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

package org.cxbox.core.dto.multivalue;

import org.cxbox.api.util.MapUtils;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MultivalueOptionType {

	HINT("hint"),
	DRILL_DOWN_TYPE("drillDownType"),
	DRILL_DOWN_LINK("drillDown"),
	SNAPSHOT_STATE("snapshotState"),
	HIDDEN("hidden"),
	PRIMARY("primary");

	private static final Map<String, MultivalueOptionType> TYPES = MapUtils
			.of(MultivalueOptionType.class, MultivalueOptionType::getValue);

	private String value;

	public static MultivalueOptionType of(final String type) {
		return TYPES.get(type);
	}

}
