/*-
 * #%L
 * IO Cxbox - Core
 * %%
 * Copyright (C) 2018 - 2019 Cxbox Contributors
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

package org.cxbox.core.util;

import org.cxbox.api.data.ResultPage;
import org.cxbox.core.dto.ResponseDTO;
import java.util.Collection;

public class ResponseBuilder {

	public static ResponseDTO build(Collection collection) {
		return new ResponseDTO(collection);
	}

	public static ResponseDTO build(Collection collection, boolean hasNext) {
		return new ResponseDTO(collection, hasNext);
	}

	public static ResponseDTO build(ResultPage page) {
		return new ResponseDTO(page);
	}

	public static ResponseDTO build() {
		return new ResponseDTO();
	}

	public static ResponseDTO build(Object data) {
		return new ResponseDTO(data);
	}

}
