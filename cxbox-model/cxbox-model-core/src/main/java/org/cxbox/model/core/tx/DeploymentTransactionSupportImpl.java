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

package org.cxbox.model.core.tx;

import org.cxbox.api.service.tx.DeploymentTransactionSupport;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


@RequiredArgsConstructor
@Service(DeploymentTransactionSupport.SERVICE_NAME)
public class DeploymentTransactionSupportImpl implements DeploymentTransactionSupport,
		ApplicationListener<ContextRefreshedEvent>, DisposableBean {

	private final PlatformTransactionManager txManager;

	private final ApplicationContext applicationContext;

	private TransactionStatus deployStatus;

	protected boolean isEnabled() {
		return false;
	}

	@PostConstruct
	public void init() {
		if (isEnabled()) {
			// цель на время деплоя открыть транзакцию для того чтобы если зависимые бины
			// при инициализации обращаются к СУБД, то подключение создавалось только один раз
			beginDeploymentTransaction();
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (isEnabled()) {
			// события могут прилетать из разных контекстов, нам интересен
			// только тот, из которого мы начали транзакцию
			if (applicationContext == event.getApplicationContext()) {
				commitDeploymentTransaction();
			}
		}
	}

	@Override
	public void destroy() {
		if (isEnabled()) {
			rollbackDeploymentTransaction();
		}
	}

	protected void beginDeploymentTransaction() {
		deployStatus = txManager.getTransaction(
				new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED)
		);
	}

	protected void commitDeploymentTransaction() {
		if (deployStatus != null) {
			txManager.commit(deployStatus);
			deployStatus = null;
		}
	}

	protected void rollbackDeploymentTransaction() {
		if (deployStatus != null) {
			txManager.commit(deployStatus);
			deployStatus = null;
		}
	}

}
