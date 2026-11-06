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

package org.cxbox.meta.validation;

import java.util.List;
import java.util.Map;
import org.cxbox.dto.MetaValidationResult;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties.Validation;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;

/**
 * Validates metadata. Register a Spring bean to add checks.
 *
 * <p>Runs on startup and metadata refresh when {@link Validation#isEnabled()} is true.
 * Problems are logged at {@link Validation#getLogLevel()}.
 * The first dev-panel login after cache invalidation also runs validation and caches the results,
 * without logging detected problems.
 *
 * <p>Validator exceptions are logged without stopping other validators or metadata loading.
 */
public interface MetaValidator {

	/**
	 * Checks metadata without modifying it. Runs synchronously without a transaction or timeout.
	 *
	 * @param widgets widgets mapped to their options JSON
	 * @return validation messages; null results and null entries are ignored
	 */
	List<MetaValidationResult> validate(List<ScreenSourceDto> screens, List<ViewSourceDTO> views,
			Map<WidgetSourceDTO, String> widgets);

	/**
	 * Null or blank titles use the Spring bean name in logs and the dev panel.
	 */
	String title();

}
