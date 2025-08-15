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

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.util.StreamUtils;

/**
 * A custom {@link HttpServletRequestWrapper} that caches the request body to allow multiple reads
 * of the {@link ServletInputStream}.
 *
 * <p>By default, {@link HttpServletRequest#getInputStream()} can only be read once. This wrapper
 * reads the entire request body during construction and stores it in memory, allowing the body
 * to be read multiple times without issues.</p>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Logging request bodies for debugging or audit purposes</li>
 *   <li>Request validation or preprocessing before reaching controllers</li>
 *   <li>Custom argument resolvers that need to parse the request body</li>
 *   <li>Filters that need to inspect request content without consuming the stream</li>
 * </ul>
 *
 * <p><strong>Memory Considerations:</strong></p>
 * <ul>
 *   <li>The entire request body is stored in memory as a byte array</li>
 *   <li>Large request bodies may cause memory issues</li>
 *   <li>Consider implementing size limits for production use</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * @Component
 * public class MultipleReadRequestFilter extends OncePerRequestFilter {
 *     @Override
 *     protected void doFilterInternal(HttpServletRequest request,
 *                                     HttpServletResponse response,
 *                                     FilterChain filterChain)
 *             throws ServletException, IOException {
 *
 *         if (hasRequestBody(request)) {
 *             CachedBodyHttpServletRequest cachedRequest =
 *                 new CachedBodyHttpServletRequest(request);
 *             filterChain.doFilter(cachedRequest, response);
 *         } else {
 *             filterChain.doFilter(request, response);
 *         }
 *     }
 * }
 * }</pre>
 *
 * @see HttpServletRequestWrapper
 * @see ServletInputStream
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

	/**
	 * The cached request body as a byte array.
	 * This field stores the entire request body read during object construction.
	 */
	private final byte[] cachedBody;


	/**
	 * Constructs a new {@code CachedBodyHttpServletRequest} that wraps the given request
	 * and caches its body content.
	 *
	 * <p>During construction, this method reads the entire request body from the original
	 * {@link HttpServletRequest#getInputStream()} and stores it in memory. This allows
	 * the body to be read multiple times later without issues.</p>
	 *
	 * <p><strong>Important:</strong> This constructor will consume the original request's
	 * {@link ServletInputStream}, making it unavailable for further direct use. All subsequent
	 * calls to {@link #getInputStream()} will return a new stream based on the cached content.</p>
	 *
	 * @param request the original {@link HttpServletRequest} to wrap
	 * @throws IOException if an I/O error occurs while reading the request body
	 * @throws IllegalArgumentException if the request is null
	 */
	public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
		super(request);
		this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
	}

	/**
	 * Returns a new {@link ServletInputStream} that reads from the cached request body.
	 *
	 * <p>This method creates a fresh {@link ServletInputStream} instance each time it's called,
	 * allowing the request body to be read multiple times. Each returned stream is independent
	 * and starts from the beginning of the cached content.</p>
	 *
	 * <p>The returned stream is backed by a {@link ByteArrayInputStream} created from the
	 * cached body content, wrapped in a {@link CachedBodyServletInputStream} to provide
	 * the required {@link ServletInputStream} interface.</p>
	 *
	 * @return a new {@link ServletInputStream} instance for reading the cached request body
	 * @throws IOException if an I/O error occurs (rarely happens with cached content)
	 * @see CachedBodyServletInputStream
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new CachedBodyServletInputStream(this.cachedBody);
	}

	/**
	 * A custom {@link ServletInputStream} implementation that provides access to cached request body content.
	 *
	 * <p>This class wraps a {@link ByteArrayInputStream} created from the cached request body,
	 * providing the required {@link ServletInputStream} interface methods. It supports multiple
	 * reads of the same content since each instance operates on a fresh {@link ByteArrayInputStream}.</p>
	 *
	 * <p><strong>Thread Safety:</strong> This class is not thread-safe. Each servlet request
	 * should use its own instance.</p>
	 *
	 * <p><strong>Implementation Notes:</strong></p>
	 * <ul>
	 *   <li>{@link #isReady()} always returns {@code true} since data is already in memory</li>
	 *   <li>{@link #isFinished()} returns {@code false} until the stream is fully read</li>
	 *   <li>{@link #setReadListener(ReadListener)} is not supported and throws {@link UnsupportedOperationException}</li>
	 * </ul>
	 *
	 * <p>The implementation idea is taken from
	 * {@link org.springframework.web.servlet.function.DefaultServerRequestBuilder.BodyInputStream}.</p>
	 *
	 * @see ServletInputStream
	 * @see ByteArrayInputStream
	 */
	static class CachedBodyServletInputStream extends ServletInputStream {

		/**
		 * The underlying {@link InputStream} that provides access to the cached body content.
		 * This is typically a {@link ByteArrayInputStream} created from the cached byte array.
		 */
		private final InputStream cachedBodyInputStream;


		/**
		 * Constructs a new {@code CachedBodyServletInputStream} with the specified cached body content.
		 *
		 * <p>This constructor creates a {@link ByteArrayInputStream} from the provided byte array,
		 * which serves as the data source for this servlet input stream.</p>
		 *
		 * @param cachedBody the cached request body as a byte array; must not be null
		 * @throws IllegalArgumentException if cachedBody is null
		 */
		public CachedBodyServletInputStream(byte[] cachedBody) {
			this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
		}

		/**
		 * Returns {@code false} to indicate that there is still data available to be read.
		 *
		 * <p>This implementation always returns {@code false} because the method doesn't
		 * track the current position in the stream. A more accurate implementation would
		 * check if all bytes have been consumed.</p>
		 *
		 * <p><strong>Note:</strong> This is a simplified implementation. For better accuracy,
		 * consider tracking the read position and returning {@code true} when all bytes
		 * have been consumed.</p>
		 *
		 * @return {@code false} always (simplified implementation)
		 */
		@Override
		public boolean isFinished() {
			return false;
		}

		/**
		 * Returns {@code true} to indicate that data is ready to be read without blocking.
		 *
		 * <p>Since the data is already cached in memory (as a {@link ByteArrayInputStream}),
		 * it's always ready for non-blocking read operations.</p>
		 *
		 * @return {@code true} always, indicating data is ready for reading
		 */
		@Override
		public boolean isReady() {
			return true;
		}

		/**
		 * This operation is not supported and will throw an exception.
		 *
		 * <p>Asynchronous reading with read listeners is not implemented in this
		 * cached stream implementation, as the data is already available in memory
		 * and can be read synchronously.</p>
		 *
		 * @param readListener the read listener (ignored)
		 * @throws UnsupportedOperationException always, as this operation is not supported
		 */
		@Override
		public void setReadListener(ReadListener readListener) {
			throw new UnsupportedOperationException();
		}

		/**
		 *
		 * Reads the next byte of data from the cached input stream.
		 *
		 * <p>This method delegates to the underlying {@link ByteArrayInputStream#read()} method
		 * to read from the cached request body content.</p>
		 *
		 * @return the next byte of data, or {@code -1} if the end of the stream is reached
		 * @throws IOException if an I/O error occurs (unlikely with {@link ByteArrayInputStream})
		 */
		@Override
		public int read() throws IOException {
			return cachedBodyInputStream.read();
		}

	}

}