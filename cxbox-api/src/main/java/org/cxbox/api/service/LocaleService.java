/*-
 * #%L
 * IO Cxbox - API
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

package org.cxbox.api.service;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


public interface LocaleService {

	String SERVICE_NAME = "localeService";

	AtomicReference<Locale> defaultLocale = new AtomicReference<>(Locale.getDefault());

	Locale getDefaultLocale();

	Set<String> getSupportedLanguages();

	boolean isLanguageSupported(String language);

}
