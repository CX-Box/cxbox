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

package org.cxbox.core.controller.http;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.RedirectStrategy;


public interface AJAXRedirectStrategy extends RedirectStrategy {

	void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException;

	boolean isAjaxRequest(HttpServletRequest request);

	String getReferrer(HttpServletRequest request);

	String getUILocation(HttpServletRequest request);

	String getSystemUrl();

	String calculateRedirectUrl(HttpServletRequest request, String url);

}
