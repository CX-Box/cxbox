/*-
 * #%L
 * IO Cxbox - Model Core
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

package org.cxbox.model.core.tx;

import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.service.tx.ITransactionStatus;
import javax.persistence.EntityManagerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;


public class CxboxJpaTransactionManagerForceActiveAware extends JpaTransactionManager {

	private final ITransactionStatus txStatus;

	public CxboxJpaTransactionManagerForceActiveAware(ApplicationContext applicationContext, CxboxBeanProperties cxboxBeanProperties, ITransactionStatus txStatus) {
		super(applicationContext.getBean(
				cxboxBeanProperties.getEntityManagerFactory(),
				EntityManagerFactory.class
		));
		this.txStatus = txStatus;
	}

	public CxboxJpaTransactionManagerForceActiveAware(EntityManagerFactory emf, ITransactionStatus txStatus) {
		super(emf);
		this.txStatus = txStatus;
	}

	@Override
	protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
		super.prepareSynchronization(status, definition);
		if (!status.isNewTransaction()) {
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapterCustom());
	}

	class TransactionSynchronizationAdapterCustom extends TransactionSynchronizationAdapter {

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE;
		}

		@Override
		public void beforeCommit(boolean readOnly) {
			txStatus.setStatus(ITransactionStatus.BEFORE_COMMIT);
		}

		@Override
		public void beforeCompletion() {
			txStatus.setStatus(ITransactionStatus.BEFORE_COMPLETION);
		}

		@Override
		public void afterCommit() {
			txStatus.setStatus(ITransactionStatus.AFTER_COMMIT);
		}

		@Override
		public void afterCompletion(int status) {
			txStatus.setStatus(ITransactionStatus.AFTER_COMPLETION);
		}

	}

}
