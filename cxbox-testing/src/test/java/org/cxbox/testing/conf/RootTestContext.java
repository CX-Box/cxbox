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

package org.cxbox.testing.conf;


import static org.mockito.Mockito.mock;

import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.api.service.tx.DeploymentTransactionSupport;
import org.cxbox.api.system.SystemSettings;
import org.cxbox.core.util.session.SessionService;
import org.springframework.context.annotation.Bean;


public class RootTestContext {

	@Bean
	public SessionService sessionService() {
		return mock(SessionService.class);
	}

	@Bean
	public DictionaryCache dictionaryCache() {
		DictionaryCache cache = mock(DictionaryCache.class);
		DictionaryCache.instance.set(cache);
		return cache;
	}

	@Bean(SystemSettings.SERVICE_NAME)
	public SystemSettings systemSettings() {
		SystemSettings systemSettings = mock(SystemSettings.class);
		SystemSettings.instance.set(systemSettings);
		return systemSettings;
	}


	@Bean(DeploymentTransactionSupport.SERVICE_NAME)
	public DeploymentTransactionSupport deploymentTransactionSupport() {
		return mock(DeploymentTransactionSupport.class);
	}


/*	@Bean
	protected PolicyEnforcer policyEnforcer() {
		PolicyEnforcer policyEnforcer = mock(PolicyEnforcer.class);
		when(policyEnforcer.check(any())).thenReturn(new ObligationSet());
		when(policyEnforcer.transform(any(), any(), any())).thenAnswer(i -> i.getArguments()[0]);
		when(policyEnforcer.transform(any(), any())).thenAnswer(i -> i.getArguments()[0]);
		return policyEnforcer;
	}*/

}
