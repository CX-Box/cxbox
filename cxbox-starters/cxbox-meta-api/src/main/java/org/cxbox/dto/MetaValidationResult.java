/*
 * © OOO "SI IKS LAB", 2022-2026
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

package org.cxbox.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Validation infers missing views from widget placement.
 * Messages without view or widget bindings are displayed under the current view.
 */
@Getter
@Setter
@Accessors(chain = true)
public class MetaValidationResult {

	private String validatorName;

	private String title;

	private String message;

	@NonNull
	private Set<String> views = new LinkedHashSet<>();

	@NonNull
	private Set<String> widgets = new LinkedHashSet<>();

	private boolean executionFailed;

	@JsonIgnore
	private String serverLogDetails;

	public static MetaValidationResult of(String message) {
		return new MetaValidationResult().setMessage(message);
	}

	public static MetaValidationResult forWidget(String widgetName, String message) {
		var problem = of(message);
		if (widgetName != null) {
			problem.getWidgets().add(widgetName);
		}
		return problem;
	}

}
