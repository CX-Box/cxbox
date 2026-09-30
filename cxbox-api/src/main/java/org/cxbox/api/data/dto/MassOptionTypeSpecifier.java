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

package org.cxbox.api.data.dto;

/**
 * Key of a value in {@link MassDTO#getOptions()}.
 * <p>
 * Platform keys are in {@link MassOptionType}.
 * A project that sends its own values of a row from its frontend declares its own enum:
 * <pre>{@code
 * @Getter
 * @RequiredArgsConstructor
 * public enum MyMassOptionType implements MassOptionTypeSpecifier {
 *     APPROVER_COMMENT("approverComment");
 *
 *     private final String value;
 * }
 *
 * String comment = mass.getOption(MyMassOptionType.APPROVER_COMMENT);
 * }</pre>
 */
public interface MassOptionTypeSpecifier {

	/**
	 * @return key of the value in the {@code options} of a {@code massIds_} element in the request
	 */
	String getValue();

}
