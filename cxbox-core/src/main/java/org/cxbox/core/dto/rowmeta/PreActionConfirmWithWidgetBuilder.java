/*
 * © OOO "SI IKS LAB", 2022-2025
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

package org.cxbox.core.dto.rowmeta;

import static org.cxbox.core.dto.rowmeta.PreAction.WITHOUT_MESSAGE;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.meta.ui.field.WidgetName;
import org.cxbox.meta.ui.field.WidgetTypeFamily;

@Slf4j
@UtilityClass
public class PreActionConfirmWithWidgetBuilder {

	/*
	Very strange!! Usually a message meant a text in body.
	And the title was in another param, but here the title is passed to message... for historical reasons "subtype", "confirmWithCustomWidget" forks this way.
	We hide this mid blowing thing in java api for `confirm` and `confirmWithWidget` using correct `title` and `text` naming (instead of hard to understand `message`)
	*/
	private static final String CONFIRM_WITH_WIDGET_WITHOUT_TITLE = WITHOUT_MESSAGE;

	/**
	 * Confirm with widget builder.
	 * <br>We keep class name very short to make inline highlights in IntelliJ IDEA non-distributive in the most common usage case
	 */
	public static final class Cfw {

		private final String widget;

		private String title;

		private String yesText;

		private String noText;

		private Cfw(String widget) {
			this.widget = widget;
		}

		/**
		 * confirm with your {@code widget}, {@code title}, and button names ({@code yesText} and {@code noText})
		 *
		 * @param widget widget name of any {@code *FormPopup.widget.json} placed on same {@code view} and having same {@code bc},
		 * as widget with button, that triggered confirmation
		 * @return builder
		 */
		static PreAction confirmWithWidgetBuilder(
				@NonNull @WidgetName(typeFamily = WidgetTypeFamily.FORM_POPUP) String widget,
				UnaryOperator<Cfw> cf) {
			var builder = new Cfw(widget);
			if (cf != null) {
				cf.apply(builder);
			}
			return builder.build();
		}

		/**
		 * @param title confirm popup {@code title}
		 * <br>
		 * <br>1. If this builder method is {@code not called} or called with {@code null} -
		 * default title from FormPopup {@code widget.json -> title} will be shown
		 * <br>2. (DEPRECATED. Use {@link Cfw#withoutTitle()}} instead) If this builder method called with non-null blank String - error will be logged
		 * @return builder
		 */
		public Cfw title(@Nullable String title) {
			try {
				if (title != null && title.isBlank()) {
					throw new IllegalStateException();
				}
			} catch (IllegalStateException e) {
				log.error(
						"title = '" + title + "'. Non null blank String has undefined behaviour. "
								+ "Please use easy to understand .withoutTitle() builder method instead!", e
				);
			}
			this.title = title;
			return this;
		}

		/**
		 * If this builder method is called - no {@code title} will be shown
		 * <br>
		 * <br> see {@link Cfw#title} java doc for details
		 *
		 * @return builder
		 */
		public Cfw withoutTitle() {
			this.title = CONFIRM_WITH_WIDGET_WITHOUT_TITLE;
			return this;
		}

		/**
		 * @param yesText text that will be shown on corresponding button in UI popup.
		 * <br>
		 * <br>If this builder method is not called or called with null - default 'Save' action text will be shown
		 * (more precisely translation defined in UI will be shown)
		 * @return builder
		 */
		public Cfw yesText(@Nullable String yesText) {
			this.yesText = yesText;
			return this;
		}

		/**
		 * @param noText text that will be shown on corresponding button in UI popup.
		 * <br>
		 * <br>If this builder method is not called or called with null - default 'Save' action text will be shown
		 * (more precisely translation defined in UI will be shown)
		 * @return builder
		 */
		public Cfw noText(@Nullable String noText) {
			this.noText = noText;
			return this;
		}

		private PreAction build() {
			return confirmWithCustomWidget(title, widget, yesText, noText);
		}

		private static PreAction confirmWithCustomWidget(@Nullable String title,
				@NonNull @WidgetName(typeFamily = WidgetTypeFamily.FORM_POPUP) String widget, @Nullable String yesButton,
				@Nullable String noButton) {
			Map<String, String> customParameters = new HashMap<>();
			customParameters.put("subtype", "confirmWithCustomWidget");
			customParameters.put("widget", widget);
			if (yesButton != null) {
				customParameters.put("yesText", yesButton);
			}
			if (noButton != null) {
				customParameters.put("noText", noButton);
			}
			/*
			Very strange!! Usually a message meant a text in body.
			And the title was in another param, but here the title is passed to message... for historical reasons "subtype", "confirmWithCustomWidget" forks this way.
			We hide this mid blowing thing in java api for `confirm` and `confirmWithWidget` using correct `title` and `text` naming (instead of hard to understand `message`)
			*/
			return PreAction.custom(title, customParameters);
		}

	}

}
