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

import org.cxbox.core.controller.param.FilterParameter.Builder;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class FilterParameters extends BaseParameterHolder<FilterParameter> {

	private FilterParameters(List<FilterParameter> parameters) {
		super(parameters, Builder.getInstance());
	}

	public static FilterParameters fromMap(Map<String, String> map) {
		List<FilterParameter> parameters = Builder.getInstance().buildParameters(map);
		return new FilterParameters(parameters);
	}

	public static FilterParameters fromList(List<FilterParameter> list) {
		return new FilterParameters(list);
	}

}
