
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

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.crudma.Crudma;
import org.cxbox.core.crudma.CrudmaActionHolder.CrudmaAction;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.CrudmaEvent;
import org.cxbox.core.crudma.CrudmaFactory;
import org.cxbox.core.crudma.InterimResult;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.ext.CrudmaGatewayInvokeExtensionProvider;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(300)
public class EventPublisherCrudmaGatewayInvokeExtensionProvider implements CrudmaGatewayInvokeExtensionProvider {

	private final ApplicationEventPublisher eventPublisher;

	private final CrudmaFactory crudmaFactory;

	@Override
	public <T> Invoker<T, RuntimeException> extendInvoker(CrudmaAction crudmaAction, Invoker<T, RuntimeException> invoker,
			boolean readOnly) {
		return () -> {
			T result = null;
			Exception exception = null;
			Function<T, ?> resultExtractor;
			if (crudmaAction.getActionType().equals(CrudmaActionType.CREATE)) {
				resultExtractor = t -> ((InterimResult) t).getDto();
			} else if (crudmaAction.getActionType().equals(CrudmaActionType.PREVIEW)) {
				resultExtractor = t -> ((InterimResult) t).getMeta();
			} else if (crudmaAction.getActionType().equals(CrudmaActionType.DELETE)) {
				BusinessComponent bc = crudmaAction.getBc();
				Crudma crudma = crudmaFactory.get(bc.getDescription());
				DataResponseDTO dataResponseDTO = crudma.get(bc);
				resultExtractor = resultDTO -> dataResponseDTO;
			} else {
				resultExtractor = Function.identity();
			}
			try {
				log.debug(crudmaAction.getDescription());
				result = invoker.invoke();
				return result;
			} catch (Exception ex) {
				exception = ex;
				throw ex;
			} finally {
				eventPublisher.publishEvent(
						new CrudmaEvent<>(
								this,
								crudmaAction,
								result == null ? null : resultExtractor.apply(result), exception
						)
				);
			}
		};
	}

}
