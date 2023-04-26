/*-
 * #%L
 * IO Cxbox - Workflow API
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

package org.cxbox.engine.workflow.validation;

import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.model.workflow.entity.WorkflowTransitionValidation;
import org.cxbox.model.workflow.entity.WorkflowableTask;
import java.util.List;

/**
 * Реализация проверки условия возможности перехода по модели переходов
 */
public interface TransitionValidator<E extends WorkflowableTask, V extends WorkflowTransitionValidation> {

	LOV getType();

	/**
	 * Выполняет проверку возможности перехода по модели переходов для активности
	 *
	 * @param task активность
	 * @param transitionValidation проверка возможности перехода
	 * @return список причин если переход невозможен или пустой список
	 */
	List<String> validate(E task, V transitionValidation);

}
