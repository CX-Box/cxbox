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

package org.cxbox.api.data.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Platform keys of the values that the frontend sends for a row of a mass action.
 * <p>
 * Mass signing and encryption ({@code options.cryptoGenerator} of a widget with a mass action)
 * sends the files it made for each row:
 * <pre>{@code
 * String signatureFileId = mass.getOption(MassOptionType.SIGNATURE_FILE_ID);
 * }</pre>
 */
@Getter
@RequiredArgsConstructor
public enum MassOptionType implements MassOptionTypeSpecifier {

	/**
	 * Id of the signature file in the file storage.
	 */
	SIGNATURE_FILE_ID("signatureFileId"),

	/**
	 * Name of the signature file.
	 */
	SIGNATURE_FILE_NAME("signatureFileName"),

	/**
	 * Id of the encrypted file in the file storage.
	 */
	ENCRYPTED_FILE_ID("encryptedFileId"),

	/**
	 * Name of the encrypted file.
	 */
	ENCRYPTED_FILE_NAME("encryptedFileName");

	private final String value;

}
