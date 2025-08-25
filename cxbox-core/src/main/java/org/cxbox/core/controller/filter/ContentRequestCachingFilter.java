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

package org.cxbox.core.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that wraps incoming {@link HttpServletRequest} instances
 * in a {@link CachedBodyHttpServletRequest} to enable multiple reads of the request body.
 *
 * <p>This filter applies to requests that have a body and are not multipart requests.
 * Filtering is controlled by {@link #shouldNotFilter(HttpServletRequest)}, which excludes:
 * <ul>
 *   <li>Requests already wrapped in {@link CachedBodyHttpServletRequest}</li>
 *   <li>Multipart requests (file uploads)</li>
 *   <li>Requests without a body (Content-Length ≤ 0 and not chunked)</li>
 * </ul>
 *
 * <p>Wrapping the request makes it possible for downstream components (controllers,
 * interceptors, argument resolvers) to safely read the request body multiple times
 * without consuming the original input stream.</p>
 *
 * <p><strong>Note:</strong> Multipart requests are excluded from wrapping to avoid
 * interfering with file upload processing. Additional endpoint exclusions can be
 * implemented by overriding {@link #shouldNotFilter(HttpServletRequest)}.</p>
 *
 * <pre>{@code
 * @Component
 * public class ContentRequestCachingFilter extends OncePerRequestFilter {
 *
 *     @Override
 *     protected void doFilterInternal(HttpServletRequest request,
 *                                     HttpServletResponse response,
 *                                     FilterChain filterChain)
 *             throws ServletException, IOException {
 *         // Always wraps since shouldNotFilter handles exclusions
 *         filterChain.doFilter(new CachedBodyHttpServletRequest(request), response);
 *     }
 *
 *     @Override
 *     protected boolean shouldNotFilter(HttpServletRequest request) {
 *         // Exclude already wrapped, multipart, or bodiless requests
 *         return request instanceof CachedBodyHttpServletRequest ||
 *                isMultipartRequest(request) ||
 *                !hasRequestBody(request);
 *     }
 * }
 * }</pre>
 *
 * @see CachedBodyHttpServletRequest
 * @see OncePerRequestFilter
 */
public class ContentRequestCachingFilter extends OncePerRequestFilter {

	/**
	 * Wraps the incoming {@link HttpServletRequest} in a {@link CachedBodyHttpServletRequest},
	 * allowing the request body to be read multiple times downstream.
	 *
	 * <p>This method unconditionally wraps the request, as all filtering logic
	 * is handled by {@link #shouldNotFilter(HttpServletRequest)}. Only requests
	 * that pass the shouldNotFilter check will reach this method.</p>
	 *
	 * @param request the original HTTP servlet request (guaranteed to need wrapping)
	 * @param response the HTTP servlet response
	 * @param filterChain the filter chain to delegate to next
	 * @throws ServletException in case of a servlet error
	 * @throws IOException in case of an I/O error reading the request body during wrapping
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		filterChain.doFilter(new CachedBodyHttpServletRequest(request), response);
	}

	/**
	 * Determines whether this filter should be skipped for the given request.
	 *
	 * <p>This method returns {@code true} (skip filtering) if any of the following conditions are met:
	 * <ul>
	 *   <li>The request is already an instance of {@link CachedBodyHttpServletRequest}</li>
	 *   <li>The request is a multipart request (Content-Type starts with "multipart/")</li>
	 *   <li>The request has no body (Content-Length ≤ 0 and Transfer-Encoding is not "chunked")</li>
	 * </ul>
	 *
	 * @param request the HTTP servlet request to check
	 * @return {@code true} to skip filtering, {@code false} to apply the filter
	 * @throws ServletException if an error occurs during the filtering decision
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return request instanceof CachedBodyHttpServletRequest ||
				(request.getContentType() != null && request.getContentType().toLowerCase(Locale.ROOT).startsWith("multipart/"))
				||
				!(request.getContentLength() > 0 || "chunked".equalsIgnoreCase(request.getHeader("Transfer-Encoding")));
	}

}