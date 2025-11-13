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

package org.cxbox.core.exception;

import org.cxbox.core.dto.BusinessError.Entity;
import org.cxbox.core.dto.rowmeta.PostAction;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;


/**
 * Core business exception for expected application errors.
 *
 * <p>This exception represents business logic violations and expected error conditions
 * that occur during normal application operation
 *
 * <p><b>Logging Behavior:</b>
 * <p>The logging level for this exception is determined as follows:
 * <ul>
 *   <li>If this exception implements {@link org.cxbox.core.exception.LoggableBusinessException},
 *       it will be logged at the standard level ({@code LogLevel.WARN})</li>
 *   <li>If this exception does not implement {@link org.cxbox.core.exception.LoggableBusinessException},
 *       it will be logged at the level specified in
 *       {@link org.cxbox.core.config.properties.LoggingProperties}</li>
 * </ul>
 *
 *
 * @see org.cxbox.core.exception.LoggableBusinessException
 * @see org.cxbox.core.config.properties.LoggingProperties
 * @see org.cxbox.core.controller.GlobalExceptionHandler
 */
@Getter
public class BusinessException extends RuntimeException {

	private List<String> popup = new ArrayList<>();

	private Entity entity = null;

	private List<PostAction> postActions = new ArrayList<>();

	public BusinessException() {
		super();
	}

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(Throwable cause) {
		super(cause);
	}

	public BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BusinessException addPopup(List<String> messages) {
		popup.addAll(messages);
		return this;
	}

	public BusinessException addPopup(String message) {
		popup.add(message);
		return this;
	}

	public BusinessException setEntity(Entity entity) {
		this.entity = entity;
		return this;
	}

	public BusinessException addPostAction(PostAction postAction) {
		this.postActions.add(postAction);
		return this;
	}

	public BusinessException setPostActions(List<PostAction> postActions) {
		this.postActions = postActions;
		return this;
	}


	@Override
	public String getMessage() {
		return StringUtils.join(popup, "\n");
	}

}
