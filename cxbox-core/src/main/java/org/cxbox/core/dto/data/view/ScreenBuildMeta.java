
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

package org.cxbox.core.dto.data.view;

import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class ScreenBuildMeta {

	private final List<String> views;

	private final Map<String, Boolean> responsibilities;

	public ScreenBuildMeta(List<String> views, Map<String, Boolean> responsibilities) {
		this.views = views;
		this.responsibilities = responsibilities;
	}

	public void addResponsibility(String viewName, boolean readOnly) {
		responsibilities.put(viewName, readOnly);
	}

	public void addView(String viewName) {
		views.add(viewName);
	}


}
