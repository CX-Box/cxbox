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

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import org.cxbox.api.exception.ServerException;
import org.cxbox.core.config.properties.APIProperties;
import org.cxbox.core.dto.ErrorResponseDTO;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.exception.BusinessIntermediateException;
import org.cxbox.core.exception.ClientException;
import org.cxbox.core.exception.UnconfirmedException;
import org.cxbox.core.exception.VersionMismatchException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Order(1)
@ControllerAdvice
public class GlobalExceptionHandler {

	@Autowired
	private APIProperties apiProperties;

	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorResponseDTO exception(Exception e) {
		UUID uuid = UUID.randomUUID();
		log.error(buildLogMessage(e, uuid), e);
		return new ErrorResponseDTO(buildResponseBody(e, uuid));
	}

	@ExceptionHandler(ClientException.class)
	public ResponseEntity<String> clientException(ClientException e) {
		UUID uuid = UUID.randomUUID();
		log.warn(buildLogMessage(e, uuid), e);
		return buildResponse(buildResponseBody(e, uuid), BAD_REQUEST);
	}

	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(value = I_AM_A_TEAPOT)
	@ResponseBody
	public ErrorResponseDTO businessException(BusinessException e) {
		log.warn(e.getMessage(), e);
		return new ErrorResponseDTO(e);
	}

	@ExceptionHandler(BusinessIntermediateException.class)
	@ResponseStatus(value = I_AM_A_TEAPOT)
	@ResponseBody
	public ErrorResponseDTO businessIntermediateException(Object data, BusinessIntermediateException e) {
		return new ErrorResponseDTO(e);
	}

	@ExceptionHandler(VersionMismatchException.class)
	@ResponseStatus(value = CONFLICT)
	@ResponseBody
	public ErrorResponseDTO versionMismatchException(VersionMismatchException e) {
		return new ErrorResponseDTO(e);
	}

	@ExceptionHandler(UnconfirmedException.class)
	@ResponseStatus(value = NOT_ACCEPTABLE)
	@ResponseBody
	public ErrorResponseDTO unconfirmedException(UnconfirmedException e) {
		return new ErrorResponseDTO(e);
	}

	private ResponseEntity<String> buildResponse(String message, HttpStatus status) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("application/json;charset=UTF-8"));
		return new ResponseEntity<>(message, headers, status);
	}

	private String buildLogMessage(Exception ex, UUID uuid) {
		StringBuilder stringBuilder = new StringBuilder();
		if (apiProperties.isTrackExceptions()) {
			stringBuilder.append(uuid.toString()).append(": ");
		}
		stringBuilder.append(ex.getMessage());
		return stringBuilder.toString();
	}

	private String buildResponseBody(Exception ex, UUID uuid) {
		StringBuilder stringBuilder = new StringBuilder();
		if (apiProperties.isTrackExceptions()) {
			stringBuilder.append(uuid.toString()).append(": ");
		}
		String message;
		if (ex instanceof ServerException) {
			message = ex.getMessage();
		} else {
			Throwable rootCause = ExceptionUtils.getRootCause(ex);
			message = rootCause == null ? ex.getClass().getSimpleName() + ": " + ex.getMessage() : rootCause.getMessage();
		}
		stringBuilder.append(message);
		if (apiProperties.isFullStackTraces()) {
			stringBuilder.append("\n");
			stringBuilder.append(ExceptionUtils.getStackTrace(ex));
		}
		return stringBuilder.toString();
	}

}
