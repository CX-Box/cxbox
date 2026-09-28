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
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.metahotreload.conf.properties.MetaConfigurationProperties.Validation;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;

/**
 * Meta validation extension point: checks meta and returns found problems as messages, which are logged with warn
 * level. Validation never breaks meta load or request: exception of validator is logged too.
 * <p>
 * Own check - register Spring bean implementing this interface and override needed methods:
 * <ul>
 *   <li>{@link #validateOnLoad} - all meta, on startup and on meta refresh</li>
 *   <li>{@link #validateOnRequest} - opened screen available for current user, once per screen opening</li>
 * </ul>
 * <pre>{@code
 * @Component
 * public class ClientMetaValidator implements MetaValidator {
 *
 *   @Override
 *   public List<String> validateOnLoad(LoadContext context) {
 *     return context.widgets().keySet().stream()
 *         .filter(widget -> widget.getTitle() == null)
 *         .map(widget -> "Widget \"" + widget.getName() + "\" has no title")
 *         .toList();
 *   }
 *
 * }
 * }</pre>
 * Validators run only when enabled by {@link Validation}, bean name ({@code clientMetaValidator}) selects validator:
 * <pre>
 * cxbox:
 *   meta:
 *     validation:
 *       enabled: true
 *       include:
 *         - clientMetaValidator
 * </pre>
 * Messages are logged by logger of validator class, so level of each validator is set by
 * {@code logging.level.<validator class>}.
 */
public interface MetaValidator {

	/**
	 * Checks all meta read from files. Called on startup and on meta refresh
	 *
	 * @return messages about found problems, empty when meta is valid
	 */
	default List<String> validateOnLoad(LoadContext context) {
		return List.of();
	}

	/**
	 * Checks meta of opened screen. Called on row-meta request for each bc of screen, so once per screen opening
	 *
	 * @return messages about found problems, empty when meta is valid
	 */
	default List<String> validateOnRequest(RequestContext context) {
		return List.of();
	}

	/**
	 * Collections are immutable copies
	 *
	 * @param screens screens read from {@code *.screen.json}
	 * @param views views read from {@code *.view.json}
	 * @param widgets widgets read from {@code *.widget.json} with their options as json
	 */
	record LoadContext(List<ScreenSourceDto> screens, List<ViewSourceDTO> views, Map<WidgetSourceDTO, String> widgets) {

		public LoadContext {
			screens = List.copyOf(screens);
			views = List.copyOf(views);
			widgets = Map.copyOf(widgets);
		}

	}

	/**
	 * All components are not null, collections are immutable copies
	 *
	 * @param bc bc from request
	 * @param screenName current screen
	 * @param screen current screen meta
	 * @param viewNames views of current screen available for current user
	 * @param views meta of {@code viewNames}
	 */
	record RequestContext(BcIdentifier bc, String screenName, ScreenDTO screen, List<String> viewNames,
			List<ViewDTO> views) {

		public RequestContext {
			viewNames = List.copyOf(viewNames);
			views = List.copyOf(views);
		}

	}

}
