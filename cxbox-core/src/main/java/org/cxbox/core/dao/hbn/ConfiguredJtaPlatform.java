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

package org.cxbox.core.dao.hbn;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.jta.JtaTransactionManager;

@RequiredArgsConstructor
@Component
public class ConfiguredJtaPlatform extends AbstractJtaPlatform {

	private final ApplicationContext applicationContext;

	@Override
	protected TransactionManager locateTransactionManager() {
		JtaTransactionManager transactionManager = applicationContext.getBean(JtaTransactionManager.class);
		return transactionManager.getTransactionManager();
	}

	@Override
	protected UserTransaction locateUserTransaction() {
		JtaTransactionManager transactionManager = applicationContext.getBean(JtaTransactionManager.class);
		return transactionManager.getUserTransaction();
	}

}
