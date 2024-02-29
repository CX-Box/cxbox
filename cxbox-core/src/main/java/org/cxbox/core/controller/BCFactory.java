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

package org.cxbox.core.controller;

import org.cxbox.core.config.properties.APIProperties;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.bc.BcHierarchy;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.BusinessComponent;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UrlPathHelper;

@Service
@RequiredArgsConstructor
public class BCFactory {

	private final APIProperties apiProperties;

	private final Set<String> operations = Set.of(
			"data", "count", "custom-action", "associate", "row-meta-new", "row-meta-empty", "row-meta"
	);

	private final BcRegistry bcRegistry;

	BcHierarchy getBcHierarchy(HttpServletRequest request) {
		Deque<String> bcList = getRequestParts(request);
		final String operation = bcList.removeFirst();
		if (!operations.contains(operation)) {
			throw new IllegalArgumentException("Operation is not supported");
		}

		String screen = bcList.removeFirst();
		if (bcList.isEmpty()) {
			throw new IllegalArgumentException("URI must contain at least one BC");
		} else if (bcList.getLast().equals("null")) {
			bcList.removeLast();
		}
		BcHierarchy bcHierarchy = null;
		while (!bcList.isEmpty()) {
			bcHierarchy = new BcHierarchy(
					screen,
					bcList.removeFirst(),
					bcList.isEmpty() ? null : bcList.removeFirst(),
					bcHierarchy
			);
		}
		return bcHierarchy;
	}

	private Deque<String> getRequestParts(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (apiProperties.getUseServletContextPath()) {
			for (String prefix : new String[]{request.getContextPath(), request.getServletPath(), "/",}) {
				uri = StringUtils.removeStart(uri, prefix);
			}
			return new LinkedList<>(Arrays.asList(uri.split("/")));
		} else {
			uri = StringUtils
					.removeStart(new UrlPathHelper().getPathWithinApplication(request), apiProperties.getPath());
			uri = StringUtils
					.removeStart(uri, "/");
			return new LinkedList<>(Arrays.asList(uri.split("/")));
		}
	}

	BusinessComponent getBusinessComponent(HttpServletRequest request, QueryParameters queryParameters) {
		return getBusinessComponent(getBcHierarchy(request), queryParameters);
	}

	public BusinessComponent getBusinessComponent(BcHierarchy bcHierarchy, QueryParameters queryParameters) {
		return new BusinessComponent(
				Optional.of(bcHierarchy).map(BcHierarchy::getId).orElse(null),
				Optional.of(bcHierarchy).map(BcHierarchy::getParent).map(BcHierarchy::getId).orElse(null),
				bcRegistry.getBcDescription(bcHierarchy.getBcName()),
				bcHierarchy,
				queryParameters
		);
	}

}
