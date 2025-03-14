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

package org.cxbox.api.data.dto.rowmeta;

import lombok.AllArgsConstructor;

/**
 * @deprecated Since 4.0.0-M11
 * use {@link Icon}
 */
@Deprecated(since = "4.0.0-M11",forRemoval = true)
@AllArgsConstructor
public enum IconCode {
	ARROW_UP_RED("arrow-up red"),
	ARROW_UP_ORANGE("arrow-up orange"),
	ARROW_DOWN_GREEN("arrow-down green");


	String code;
}
