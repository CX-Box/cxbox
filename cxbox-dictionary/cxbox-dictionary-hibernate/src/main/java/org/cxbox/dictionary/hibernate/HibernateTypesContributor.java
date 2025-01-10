/*
 * © OOO "SI IKS LAB", 2022-2024
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

package org.cxbox.dictionary.hibernate;

import java.util.ServiceLoader;
import org.cxbox.dictionary.DictionaryClassProvider;
import org.hibernate.HibernateException;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.service.ServiceRegistry;

public class HibernateTypesContributor implements TypeContributor {

	@Override
	public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
		ConfigurationService configurationService = serviceRegistry.getService(ConfigurationService.class);
		Boolean enableTypesContributor = (Boolean) configurationService.getSetting(
				"org.cxbox.dictionary.enable_types_contributor",
				value -> {
					if (value instanceof Boolean) {
						return value;
					}
					if (value instanceof String stringValue) {
						return Boolean.parseBoolean(stringValue);
					}
					throw new HibernateException(
							String.format(
									"The value [%s] of the [%s] setting is not supported!",
									value,
									"org.cxbox.dictionary.enable_types_contributor"
							)
					);
				}
		);
		if (Boolean.FALSE.equals(enableTypesContributor)) {
			return;
		}

		ServiceLoader<DictionaryClassProvider> serviceLoader = ServiceLoader.load(DictionaryClassProvider.class);
		for (DictionaryClassProvider provider : serviceLoader) {
			try {
				var dictClass = provider.getDictionaryType();
				typeContributions.contributeType(new DictionaryType(dictClass), dictClass.getName());
			} catch (NoClassDefFoundError e) {
				//
			}
		}
	}

}
