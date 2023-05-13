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

import static org.cxbox.core.config.properties.APIProperties.CXBOX_API_PATH_SPEL;

import org.cxbox.core.file.dto.CxboxResponseDTO;
import org.cxbox.core.file.dto.FileDownloadDto;
import org.cxbox.core.file.dto.FileUploadDto;
import org.cxbox.core.file.service.CxboxFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(CXBOX_API_PATH_SPEL + "/file")
@ConditionalOnMissingBean(CxboxFileController.class)
public class CxboxFileControllerSimple implements CxboxFileController {

	private final CxboxFileService cxboxFileService;

	@Override
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CxboxResponseDTO<FileUploadDto> upload(MultipartFile file, String source) {
		String id = cxboxFileService.upload(file, source);
		return new CxboxResponseDTO<FileUploadDto>()
				.setData(new FileUploadDto(id, file.getOriginalFilename(), file.getContentType()));
	}

	@Override
	@GetMapping
	public HttpEntity<byte[]> download(String id, String source, boolean preview) {
		FileDownloadDto file = cxboxFileService.download(id, source);
		return buildFileHttpEntity(file.getBytes(), file.getName(), file.getType(), preview);
	}

	@Override
	@DeleteMapping
	public CxboxResponseDTO<Void> remove(String id, String source) {
		cxboxFileService.remove(id, source);
		return new CxboxResponseDTO<>();
	}

}
