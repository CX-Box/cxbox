/*-
 * #%L
 * IO Cxbox - Core
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.core.controller.param;

import org.cxbox.api.util.MapUtils;
import java.util.Map;
import java.util.Optional;


public enum DateStep {
	DAY,
	MONTH,
	QUARTER,
	YEAR;

	private static final Map<String, DateStep> STEPS = MapUtils.of(
			DateStep.class, DateStep::name
	);

	public static DateStep of(String value, DateStep defaultValue) {
		return Optional.ofNullable(value).map(String::toUpperCase)
				.map(STEPS::get).orElse(defaultValue);
	}

}
