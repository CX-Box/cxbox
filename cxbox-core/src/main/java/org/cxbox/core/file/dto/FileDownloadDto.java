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

package org.cxbox.core.file.dto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@Getter
public class FileDownloadDto {

	private final Supplier<InputStream> content;

	private final String name;

	private final long length;

	private final String type;

	/**
	 * deprecated. use {@link #FileDownloadDto(Supplier, long, String, String)}
	 *
	 * @param bytes file content
	 * @param name file name
	 * @param type file type
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	public FileDownloadDto(byte[] bytes, String name, String type) {
		this.content = bytes.length == 0 ? null : () -> new ByteArrayInputStream(bytes);
		this.length = bytes.length;
		this.name = name;
		this.type = type;
	}

	/**
	 * @param content file content
	 * @param name file name
	 * @param type`file type
	 */
	public FileDownloadDto(@NonNull Supplier<InputStream> content, long length, String name, String type) {
		this.content = content;
		this.length = length;
		this.name = name;
		this.type = type;
	}

	/**
	 * deprecated. use {@link #getContent()}
	 *
	 * @return file content
	 */
	@Deprecated(since = "4.0.0-M12", forRemoval = true)
	@SneakyThrows
	public byte[] getBytes() {
		return content == null ? new byte[0] : content.get().readAllBytes();
	}

}
