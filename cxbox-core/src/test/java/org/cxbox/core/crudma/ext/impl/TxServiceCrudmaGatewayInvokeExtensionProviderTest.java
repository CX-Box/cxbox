/*-
 * #%L
 * IO Cxbox - Core
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

package org.cxbox.core.crudma.ext.impl;

import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.crudma.CrudmaActionHolder.CrudmaAction;
import org.cxbox.core.crudma.CrudmaActionHolder.SimpleCrudmaAction;
import org.cxbox.core.crudma.CrudmaActionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TxServiceCrudmaGatewayInvokeExtensionProviderTest {

	@Mock
	TransactionService txService;

	@Mock
	Logger log;

	@InjectMocks
	TxServiceCrudmaGatewayInvokeExtensionProvider txServiceCrudmaGatewayInvokeExtensionProvider;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testExtendInvoker() {
		Executable executable = () -> {
			when(txService.invokeInNewTx(any())).thenReturn(false);
			when(txService.invokeInNewRollbackOnlyTx(any())).thenReturn(true);
		};
		Assertions.assertDoesNotThrow(executable);
		CrudmaAction crudmaAction = new SimpleCrudmaAction(CrudmaActionType.INVOKE);
		Invoker<Object, RuntimeException> result;
		result = txServiceCrudmaGatewayInvokeExtensionProvider.extendInvoker(crudmaAction, null, true);
		Assertions.assertEquals(true, result.invoke());
		result = txServiceCrudmaGatewayInvokeExtensionProvider.extendInvoker(crudmaAction, null, false);
		Assertions.assertEquals(false, result.invoke());
	}

}
