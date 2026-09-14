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

package org.cxbox.core.service.drilldown.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.test.util.TestResponseDto;
import org.junit.jupiter.api.Test;

class PlatformDrilldownFilterServiceTest {

	private static final DtoField<TestResponseDto, String> HINT = new DtoField<>("customField", String.class);

	private final PlatformDrilldownFilterService service = new PlatformDrilldownFilterService();

	@Test
	void hintUsesContainsLikeFrontendColumnFilter() {
		String filter = service.hint(HINT, "Test 123");

		assertEquals("customField.contains=Test 123", URLDecoder.decode(filter, StandardCharsets.UTF_8));
	}

	@Test
	void hintWithNullValueGivesNoFilter() {
		assertNull(service.hint(HINT, null));
	}

}
