/*-
 * #%L
 * IO Cxbox - Core
 * %%
 * Copyright (C) 2018 - 2021 Cxbox Contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.cxbox.core.file;

import org.cxbox.core.file.dto.FileDownloadDto;
import org.cxbox.core.file.service.CxboxFileService;
import org.cxbox.core.file.service.CxboxFileServiceSimple;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CxboxFileServiceSimpleTest {

	@Test
	@SneakyThrows
	void download() {
		String fileStorage = Paths.get(getClass().getResource("").toURI()).toAbsolutePath().toString();
		String jacksonStorage = Paths.get(getClass().getResource("../../jackson").toURI()).toAbsolutePath().toString();
		CxboxFileService service = new CxboxFileServiceSimple(fileStorage);
		assertThatThrownBy(() -> {
			FileDownloadDto dtoOutsideOfDirectory = service.download("../../jackson/InputField.json", null);
		}).isInstanceOf(NoSuchFileException.class);
		CxboxFileService jacksonService = new CxboxFileServiceSimple(jacksonStorage);
		FileDownloadDto jacksonDto = jacksonService.download("InputField.json", null);
		assertThat(jacksonDto.getName()).isEqualTo("InputField");
		FileDownloadDto dto = service.download("download.txt", null);
		assertThat(dto.getName()).isEqualTo("download");
	}
}
