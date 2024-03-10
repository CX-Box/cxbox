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

package org.cxbox.model.core.service;

import org.cxbox.api.service.LocaleService;
import org.cxbox.model.core.api.Translatable;
import org.cxbox.model.core.api.Translation;
import org.cxbox.model.core.api.TranslationId;
import org.cxbox.model.core.api.TranslationService;
import org.cxbox.model.core.entity.BaseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

	private final LocaleService localeService;

	@Override
	public <L extends Translation<E, L>, E extends BaseEntity & Translatable<E, L>> List<String> getMissingTranslations(
			E translatable) {
		Set<String> supportedLanguages = localeService.getSupportedLanguages();
		List<String> missingLanguages = new ArrayList<>(supportedLanguages);
		Map<String, L> translations = translatable.getTranslations();
		missingLanguages.removeAll(translations.keySet());
		return missingLanguages;
	}

	@SneakyThrows
	@Override
	public <L extends Translation<E, L>, E extends BaseEntity & Translatable<E, L>> List<L> populate(E translatable) {
		List<String> missingLanguages = getMissingTranslations(translatable);
		Optional<L> template = translatable.getTranslation(localeService.getDefaultLocale().getLanguage(), null);
		Class<L> cls = translatable.getTranslationType();
		List<L> result = new ArrayList<>();
		for (String language : missingLanguages) {
			L copy = template.map(L::copyTranslation).orElse(cls.newInstance());
			copy.setPrimaryEntity(translatable);
			copy.setTranslationId(new TranslationId(language));
			translatable.addTranslation(copy);
			result.add(copy);
		}
		return result;
	}

}
