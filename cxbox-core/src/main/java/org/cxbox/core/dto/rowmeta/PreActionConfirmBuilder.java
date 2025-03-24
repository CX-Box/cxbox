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
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.core.dto.rowmeta.PreAction.PreActionBuilder;

@Slf4j
@UtilityClass
public class PreActionConfirmBuilder {

	private static final String CONFIRM_WITHOUT_TITLE = "";

	private static final String CONFIRM_WITHOUT_TEXT = WITHOUT_MESSAGE;

	/**
	 * Confirm builder.
	 * <br>We keep class name very short to make inline highlights in IntelliJ IDEA non-distributive in the most common usage case
	 */
	public static final class Cf {

		private String title;

		private String text;

		private String yesText;

		private String noText;

		private Cf() {
		}

		/**
		 * confirm with your {@code title}, {@code text}, and button names ({@code yesText} and {@code noText})
		 *
		 * @return builder
		 */
		static PreAction confirmBuilder(UnaryOperator<Cf> cf) {
			var builder = new Cf();
			if (cf != null) {
				cf.apply(builder);
			}
			return builder.build();
		}

		/**
		 * @param title confirm popup {@code title}
		 * <br>
		 * <br>1. If this builder method is {@code not called} or called with {@code null} -
		 * default 'Are you sure?' {@code title} will be shown (actually its translation defined in Frontend i18n)
		 * <br>2. (DEPRECATED. Use {@link Cf#withoutTitle()})
		 * If this builder method called with non-null blank String - error will be logged
		 * @return builder
		 */
		public Cf title(@Nullable String title) {
			try {
				if (title != null && title.isBlank()) {
					throw new IllegalStateException();
				}
			} catch (IllegalStateException e) {
				log.error("title = '" + title + "'. Non null blank String has undefined behaviour."
						+ "Please use easy to understand .withoutTitle() builder method instead!", e);
			}
			this.title = title;
			return this;
		}

		/**
		 * If this builder method is called - no {@code title} will be shown
		 * <br>
		 * <br> see {@link Cf#title} java doc for details
		 *
		 * @return builder
		 */
		public Cf withoutTitle() {
			this.title = CONFIRM_WITHOUT_TITLE;
			return this;
		}

		/**
		 * @param text confirm popup {@code text} text
		 * <br>
		 * <br>1. If this builder method is {@code not called} or called with {@code null} - {@code text} will be auto-generated by backend from {@link PreActionType#CONFIRMATION} template.
		 * <br>2. (DEPRECATED. Use {@link Cf#withoutText()} instead) If this builder method called with non-null blank String - error will be logged
		 * @return builder
		 */
		public Cf text(@Nullable String text) {
			try {
				if (text != null && text.isBlank()) {
					throw new IllegalStateException();
				}
			} catch (IllegalStateException e) {
				log.error("text = '" + text + "'. Non null blank String has undefined behaviour. "
						+ "Please use easy to understand .withoutText() builder method instead!", e);
			}
			this.text = text;
			return this;
		}

		/**
		 * If this builder method is called - no {@code text} will be shown
		 * <br>
		 * <br> see {@link Cf#text} java doc for details
		 *
		 * @return builder
		 */
		public Cf withoutText() {
			this.text = CONFIRM_WITHOUT_TEXT;
			return this;
		}

		/**
		 * @param yesText text that will be shown on corresponding button in UI popup.
		 * <br>
		 * <br>If this builder method is not called or called with null - default 'Save' action text will be shown
		 * (more precisely translation defined in UI will be shown)
		 * @return builder
		 */
		public Cf yesText(@Nullable String yesText) {
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
		public Cf noText(@Nullable String noText) {
			this.noText = noText;
			return this;
		}

		private PreAction build() {
			return confirm(text, title, yesText, noText);
		}

		private static PreAction confirm(@Nullable String text, @Nullable String title, @Nullable String yesButton,
				@Nullable String noButton) {
			PreActionBuilder preActionBuilder = PreAction.builder().preActionType(PreActionType.CONFIRMATION);
			Map<String, String> customParameters = new HashMap<>();

			preActionBuilder.message(text);

			if (title != null) {
				customParameters.put("messageContent", title);
			}
			if (yesButton != null) {
				customParameters.put("okText", yesButton);
			}
			if (noButton != null) {
				customParameters.put("cancelText", noButton);
			}
			return preActionBuilder
					.customParameters(customParameters)
					.build();
		}

	}

}
