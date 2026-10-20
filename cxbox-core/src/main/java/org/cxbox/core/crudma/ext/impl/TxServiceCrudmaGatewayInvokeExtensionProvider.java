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

package org.cxbox.core.crudma.ext.impl;

import static org.cxbox.api.util.i18n.ErrorMessageSource.errorMessage;

import lombok.RequiredArgsConstructor;
import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.crudma.CrudmaActionHolder.CrudmaAction;
import org.cxbox.core.crudma.ext.CrudmaGatewayInvokeExtensionProvider;
import org.cxbox.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
@Order(200)
@Slf4j
public class TxServiceCrudmaGatewayInvokeExtensionProvider implements CrudmaGatewayInvokeExtensionProvider {

	private final TransactionService txService;

	@Override
	public <T> Invoker<T, RuntimeException> extendInvoker(CrudmaAction crudmaAction, Invoker<T, RuntimeException> invoker,
			boolean readOnly) {
		if (crudmaAction.isWithoutTransaction()) {
			refuseWhenResourceBound(crudmaAction);
			return () -> {
				refuseWhenResourceBound(crudmaAction);
				return invoker.invoke();
			};
		}
		return () -> {
			if (readOnly) {
				log.debug("Open read-only transaction for crudmaAction: " + crudmaAction);
				return txService.invokeInNewRollbackOnlyTx(invoker);
			}
			log.debug("Open read-write transaction for crudmaAction: " + crudmaAction);
			return txService.invokeInNewTx(invoker);
		};
	}

	private void refuseWhenResourceBound(CrudmaAction crudmaAction) {
		if (isTransactionResourceBound()) {
			log.warn(
					"Action {} is refused: a transaction or an entity manager is already bound to the thread",
					crudmaAction.getName()
			);
			throw new BusinessException().addPopup(
					errorMessage("error.action_without_transaction_resource_bound", crudmaAction.getName())
			);
		}
	}

	private static boolean isTransactionResourceBound() {
		return TransactionSynchronizationManager.isActualTransactionActive()
				|| TransactionSynchronizationManager.isSynchronizationActive()
				|| TransactionSynchronizationManager.getResourceMap().values().stream()
				.anyMatch(EntityManagerHolder.class::isInstance);
	}

}
