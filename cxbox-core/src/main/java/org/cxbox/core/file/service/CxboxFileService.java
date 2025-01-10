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

package org.cxbox.core.file.service;

import java.io.IOException;
import org.cxbox.core.file.dto.FileDownloadDto;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

public interface CxboxFileService {

	/**
	 * @param file entity to be saved
	 * @param source (deprecated)
	 * @return unique file id
	 */
	<D extends FileDownloadDto> String upload(@NonNull D file, @Nullable String source);

	/**
	 * @param file entity to be saved
	 * @param source (deprecated)
	 * @return unique file id
	 */
	@SneakyThrows
	default String upload(@NonNull MultipartFile file, @Nullable String source) {
		return upload(new FileDownloadDto(() -> {
			try {
				return file.getInputStream();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}, file.getSize(), file.getOriginalFilename(), file.getContentType()), source);
	}

	/**
	 * @param id unique file id, that was returned by upload(...) method
	 * @param source (deprecated)
	 * @return file entity
	 */
	FileDownloadDto download(@NonNull String id, @Nullable String source);


	/**
	 * @param id unique file id, that was returned by upload(...) method
	 * @param source (deprecated)
	 */
	void remove(@NonNull String id, @Nullable String source);

}
