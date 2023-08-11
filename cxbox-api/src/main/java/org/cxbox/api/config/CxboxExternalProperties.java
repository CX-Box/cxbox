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

package org.cxbox.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cxbox.external")
public class CxboxExternalProperties {

	/**
	 * In BcStateCrudmaGatewayInvokeExtensionProvider we set bcState for any bc type, but use this cache only for InnerBcDescription. When this property = TRUE, then it turns off bcState for non InnerBcDescription - so you can implement your own BcStateCrudmaGatewayInvokeExtensionProvider for other bc types
	 */
	private Boolean useStandardBcStateForInnerBcOnly = Boolean.FALSE;
}
