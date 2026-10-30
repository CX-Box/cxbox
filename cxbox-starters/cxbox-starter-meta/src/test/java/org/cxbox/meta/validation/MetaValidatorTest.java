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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cxbox.api.data.BcIdentifier;
import org.cxbox.meta.data.ScreenDTO;
import org.cxbox.meta.data.ViewDTO;
import org.cxbox.meta.metahotreload.dto.ScreenSourceDto;
import org.cxbox.meta.metahotreload.dto.ViewSourceDTO;
import org.cxbox.meta.metahotreload.dto.WidgetSourceDTO;
import org.cxbox.meta.validation.MetaValidator.LoadContext;
import org.cxbox.meta.validation.MetaValidator.RequestContext;
import org.junit.jupiter.api.Test;

class MetaValidatorTest {

	private final ScreenSourceDto screen = new ScreenSourceDto();

	private final ViewSourceDTO sourceView = new ViewSourceDTO();

	private final WidgetSourceDTO widget = new WidgetSourceDTO();

	private final ViewDTO view = new ViewDTO();

	@Test
	void loadContextCannotBeChanged() {
		var screens = new ArrayList<>(List.of(screen));
		var views = new ArrayList<>(List.of(sourceView));
		var widgets = new HashMap<>(Map.of(widget, "{}"));
		var context = new LoadContext(screens, views, widgets);

		assertThatThrownBy(() -> context.screens().clear()).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> context.views().clear()).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> context.widgets().clear()).isInstanceOf(UnsupportedOperationException.class);

		assertThat(context.screens()).containsExactly(screen);
		assertThat(context.views()).containsExactly(sourceView);
		assertThat(context.widgets()).containsExactly(entry(widget, "{}"));
		assertThat(screens).containsExactly(screen);
		assertThat(views).containsExactly(sourceView);
		assertThat(widgets).containsExactly(entry(widget, "{}"));
	}

	@Test
	void loadContextIsNotChangedWithSource() {
		var screens = new ArrayList<>(List.of(screen));
		var views = new ArrayList<>(List.of(sourceView));
		var widgets = new HashMap<>(Map.of(widget, "{}"));
		var context = new LoadContext(screens, views, widgets);

		screens.clear();
		views.clear();
		widgets.clear();

		assertThat(context.screens()).containsExactly(screen);
		assertThat(context.views()).containsExactly(sourceView);
		assertThat(context.widgets()).containsExactly(entry(widget, "{}"));
	}

	@Test
	void requestContextCannotBeChanged() {
		var viewNames = new ArrayList<>(List.of("view"));
		var views = new ArrayList<>(List.of(view));
		var context = new RequestContext(mock(BcIdentifier.class), "screen", new ScreenDTO(), viewNames, views);

		assertThatThrownBy(() -> context.viewNames().clear()).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> context.views().clear()).isInstanceOf(UnsupportedOperationException.class);

		assertThat(context.viewNames()).containsExactly("view");
		assertThat(context.views()).containsExactly(view);
		assertThat(viewNames).containsExactly("view");
		assertThat(views).containsExactly(view);
	}

	@Test
	void requestContextIsNotChangedWithSource() {
		var viewNames = new ArrayList<>(List.of("view"));
		var views = new ArrayList<>(List.of(view));
		var context = new RequestContext(mock(BcIdentifier.class), "screen", new ScreenDTO(), viewNames, views);

		viewNames.clear();
		views.clear();

		assertThat(context.viewNames()).containsExactly("view");
		assertThat(context.views()).containsExactly(view);
	}

}
