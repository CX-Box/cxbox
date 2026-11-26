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

package org.cxbox.core.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.sun.management.UnixOperatingSystemMXBean;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.cxbox.core.file.controller.AbstractCxboxFileController;
import org.cxbox.core.file.dto.FileDownloadDto;
import org.cxbox.core.file.service.CxboxFileService;
import org.cxbox.core.file.service.CxboxFileServiceSimple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * The download response closes the file stream when the body ends: after the file is sent, when the client leaves
 * and when reading the storage fails. A failure of close() itself does not change the response.
 */
class FileDownloadStreamTest {

	private static final byte[] CONTENT = "hello".getBytes(UTF_8);

	@Test
	void closesStreamAfterFileIsSent() throws IOException {
		TrackingInputStream content = new TrackingInputStream(CONTENT);

		ByteArrayOutputStream client = new ByteArrayOutputStream();
		download(content).getBody().writeTo(client);

		assertThat(client.toByteArray()).isEqualTo(CONTENT);
		assertThat(content.closeCount).isEqualTo(1);
	}

	@Test
	void closesStreamWhenClientLeaves() {
		TrackingInputStream content = new TrackingInputStream(CONTENT);
		StreamingResponseBody body = download(content).getBody();

		assertThatThrownBy(() -> body.writeTo(new ClientThatLeft()))
				.isInstanceOf(AsyncRequestNotUsableException.class);

		assertThat(content.closeCount).isEqualTo(1);
	}

	@Test
	void closesStreamAndKeepsErrorWhenStorageFailsDuringDownload() {
		TrackingInputStream content = new TrackingInputStream(CONTENT, new IOException("Connection reset"), null);
		StreamingResponseBody body = download(content).getBody();

		assertThatThrownBy(() -> body.writeTo(new ByteArrayOutputStream()))
				.isInstanceOf(IOException.class)
				.hasMessage("Connection reset");

		assertThat(content.closeCount).isEqualTo(1);
	}

	@Test
	void ignoresCloseFailureAfterFileIsSent() throws IOException {
		TrackingInputStream content = new TrackingInputStream(CONTENT, null, new IOException("close failed"));

		ByteArrayOutputStream client = new ByteArrayOutputStream();
		download(content).getBody().writeTo(client);

		assertThat(client.toByteArray()).isEqualTo(CONTENT);
		assertThat(content.closeCount).isEqualTo(1);
	}

	@Test
	void keepsDownloadErrorWhenCloseFails() {
		TrackingInputStream content = new TrackingInputStream(CONTENT, null, new IOException("close failed"));
		StreamingResponseBody body = download(content).getBody();

		assertThatThrownBy(() -> body.writeTo(new ClientThatLeft()))
				.isInstanceOf(AsyncRequestNotUsableException.class)
				.hasNoSuppressedExceptions();

		assertThat(content.closeCount).isEqualTo(1);
	}

	@Test
	void downloadsFromFileSystemStorageLeaveNoOpenFile(@TempDir Path folder) throws IOException {
		assumeTrue(ManagementFactory.getOperatingSystemMXBean() instanceof UnixOperatingSystemMXBean,
				"open file descriptors are counted on Unix only");
		UnixOperatingSystemMXBean os = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		CxboxFileService service = new CxboxFileServiceSimple(folder.toString());
		String id = service.upload(
				new FileDownloadDto(() -> new ByteArrayInputStream(CONTENT), CONTENT.length, "report.txt", "text/plain"),
				null
		);
		AbstractCxboxFileController controller = controller(service);

		long before = os.getOpenFileDescriptorCount();
		for (int i = 0; i < 50; i++) {
			ByteArrayOutputStream client = new ByteArrayOutputStream();
			controller.download(id, null, false).getBody().writeTo(client);
			assertThat(client.toByteArray()).isEqualTo(CONTENT);
		}

		assertThat(os.getOpenFileDescriptorCount() - before).isLessThan(10);
	}

	private static HttpEntity<StreamingResponseBody> download(TrackingInputStream content) {
		return controller(storage(() -> content)).download("id", null, false);
	}

	private static AbstractCxboxFileController controller(CxboxFileService service) {
		AbstractCxboxFileController controller = new AbstractCxboxFileController() {
		};
		ReflectionTestUtils.setField(controller, "cxboxFileService", service);
		return controller;
	}

	private static CxboxFileService storage(Supplier<InputStream> content) {
		return new CxboxFileService() {
			@Override
			public <D extends FileDownloadDto> String upload(D file, String source) {
				throw new UnsupportedOperationException();
			}

			@Override
			public FileDownloadDto download(String id, String source) {
				return new FileDownloadDto(content, CONTENT.length, "report.txt", "text/plain");
			}

			@Override
			public void remove(String id, String source) {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Counts close() calls. Can fail on read (the storage broke) and on close.
	 */
	private static class TrackingInputStream extends InputStream {

		private final InputStream data;

		private final IOException readFailure;

		private final IOException closeFailure;

		private int closeCount;

		TrackingInputStream(byte[] content) {
			this(content, null, null);
		}

		TrackingInputStream(byte[] content, IOException readFailure, IOException closeFailure) {
			this.data = new ByteArrayInputStream(content);
			this.readFailure = readFailure;
			this.closeFailure = closeFailure;
		}

		@Override
		public int read() throws IOException {
			if (readFailure != null) {
				throw readFailure;
			}
			return data.read();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (readFailure != null) {
				throw readFailure;
			}
			return data.read(b, off, len);
		}

		@Override
		public void close() throws IOException {
			closeCount++;
			if (closeFailure != null) {
				throw closeFailure;
			}
		}

	}

	/**
	 * The response of a client that left: Spring MVC reports it with AsyncRequestNotUsableException.
	 */
	private static class ClientThatLeft extends OutputStream {

		@Override
		public void write(int b) throws IOException {
			throw new AsyncRequestNotUsableException("ServletOutputStream failed to write: Broken pipe");
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			throw new AsyncRequestNotUsableException("ServletOutputStream failed to write: Broken pipe");
		}

	}

}
