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

package org.cxbox.api.service.tx;

import org.cxbox.api.util.Invoker;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public interface TransactionService {

	String SERVICE_NAME = "transactionService";

	@Transactional(propagation = Propagation.REQUIRED)
	<T, E extends Throwable> T invokeInTx(Invoker<T, E> invoker) throws E;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	<T, E extends Throwable> T invokeInNewTx(Invoker<T, E> invoker) throws E;

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	<T, E extends Throwable> T invokeNoTx(Invoker<T, E> invoker) throws E;

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	<T, E extends Throwable> T invokeInNewROTx(Invoker<T, E> invoker) throws E;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	<T, E extends Throwable> T invokeInNewRollbackOnlyTx(Invoker<T, E> invoker) throws E;

	void setRollbackOnly();

	boolean isRollbackOnly();

	boolean isActive();

	<T, E extends RuntimeException> void invokeAfterCompletion(Invoker<T, E> invoker) throws E;

	<T> T woAutoFlush(Invoker<T, RuntimeException> invoker);

	void flush();

}
