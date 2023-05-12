
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

import org.cxbox.api.service.tx.ITransactionStatus;
import org.cxbox.model.core.config.TransactionScope;
import org.springframework.stereotype.Service;


@Service
@TransactionScope
public class TransactionStatusImpl implements ITransactionStatus {

	private int status;

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public boolean isBeforeCommit() {
		return status == BEFORE_COMMIT;
	}

	@Override
	public boolean isBeforeCompletion() {
		return status == BEFORE_COMPLETION;
	}

	@Override
	public boolean isAfterCommit() {
		return status == AFTER_COMMIT;
	}

	@Override
	public boolean isAfterCompletion() {
		return status == AFTER_COMPLETION;
	}

	@Override
	public boolean isCommitting() {
		return isBeforeCommit() || isBeforeCompletion();
	}


}
