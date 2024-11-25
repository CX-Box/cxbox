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

package org.cxbox.core.file.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import org.cxbox.core.file.dto.CxboxResponseDTO;
import org.cxbox.core.file.dto.FileUploadDto;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Add
 * <pre>{@code
 * @RestController
 * @RequestMapping(CXBOX_API_PATH_SPEL + "/file")
 * }</pre>
 * on implementation class
 */
public interface CxboxFileController {


	/**
	 * Add
	 * <pre>{@code
	 * @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	 * }</pre>
	 * on implementation class.
	 * You can extend api for /file endpoint (declaring method with another signature), so we are not placing @PostMapping on interface to avoid endpoints clash
	 */
	CxboxResponseDTO<? extends FileUploadDto> upload(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "source", required = false) String source);


	/**
	 * Add
	 * <pre>{@code
	 * @GetMapping
	 * }</pre>
	 * on implementation class.
	 * You can extend api for /file endpoint (declaring method with another signature), so we are not placing @PostMapping on interface to avoid endpoints clash.
	 * <pre></pre>
	 * See buildFileHttpEntity example of constructing response for this method
	 */
	HttpEntity<StreamingResponseBody> download(
			@RequestParam("id") String id,
			@RequestParam(value = "source", required = false) String source,
			@RequestParam(value = "preview", required = false, defaultValue = "false") boolean preview);

	/**
	 * Add
	 * <pre>{@code
	 * @DeleteMapping
	 * }</pre>
	 * on implementation class.
	 * You can extend api for /file endpoint (declaring method with another signature), so we are not placing @PostMapping on interface to avoid endpoints clash
	 */
	CxboxResponseDTO<Void> remove(
			@RequestParam("id") String id,
			@RequestParam("source") String source);


	default HttpEntity<StreamingResponseBody> buildFileHttpEntity(InputStream content, Long length, String fileName,
			String fileType, boolean preview) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(
				HttpHeaders.CONTENT_DISPOSITION,
				ContentDisposition.builder(preview ? "inline" : "attachment")
						.filename(fileName, StandardCharsets.UTF_8)
						.build()
						.toString()
		);
		headers.setContentType(getMediaType(fileType));
		headers.setContentLength(length);
		return ResponseEntity.ok()
				.headers(headers)
				.body(outputStream -> copy(content, outputStream, getChunkSize()));
	}

	default int getChunkSize() {
		int mib = 1048576;
		return mib * 5;
	}

	default long copy(final InputStream inputStream, final OutputStream outputStream, final int bufferSize)
			throws IOException {
		return copyLarge(inputStream, outputStream, new byte[bufferSize]);
	}

	default long copyLarge(final InputStream inputStream, final OutputStream outputStream, final byte[] buffer)
			throws IOException {
		Objects.requireNonNull(inputStream, "inputStream");
		Objects.requireNonNull(outputStream, "outputStream");
		long count = 0;
		int n;
		while (-1 != (n = inputStream.read(buffer))) {
			outputStream.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	default MediaType getMediaType(final String type) {
		try {
			return MediaType.parseMediaType(type);
		} catch (InvalidMediaTypeException e) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

}
