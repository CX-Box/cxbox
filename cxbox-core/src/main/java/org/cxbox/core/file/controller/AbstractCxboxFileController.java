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

import org.cxbox.core.file.dto.CxboxResponseDTO;
import org.cxbox.core.file.dto.FileDownloadDto;
import org.cxbox.core.file.dto.FileUploadDto;
import org.cxbox.core.file.service.CxboxFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Slf4j
public abstract class AbstractCxboxFileController implements CxboxFileController {

	@Autowired
	private CxboxFileService cxboxFileService;

	@Override
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CxboxResponseDTO<FileUploadDto> upload(MultipartFile file, String source) {
		String id = cxboxFileService.upload(file, source);
		return new CxboxResponseDTO<FileUploadDto>()
				.setData(new FileUploadDto(id, file.getOriginalFilename(), file.getContentType()));
	}

	@Override
	@GetMapping
	public HttpEntity<StreamingResponseBody> download(String id, String source, boolean preview) {
		FileDownloadDto file = cxboxFileService.download(id, source);
		return buildFileHttpEntity(file.getContent().get(), file.getLength(), file.getName(), file.getType(), preview);
	}

	@Override
	@DeleteMapping
	public CxboxResponseDTO<Void> remove(String id, String source) {
		cxboxFileService.remove(id, source);
		return new CxboxResponseDTO<>();
	}

}
