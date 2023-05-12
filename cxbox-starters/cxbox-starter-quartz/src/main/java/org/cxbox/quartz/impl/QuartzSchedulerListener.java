
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

package org.cxbox.quartz.impl;

import org.cxbox.api.service.tx.TransactionService;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.quartz.model.ScheduledJob;
import org.quartz.listeners.SchedulerListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Component("quartzSchedulerListener")
public class QuartzSchedulerListener extends SchedulerListenerSupport {

	@Autowired
	private JpaDao jpaDao;

	@Autowired
	private TransactionService txService;

	@Autowired
	@Lazy
	private SchedulerService schedulerService;

	@Override
	public void schedulerStarted() {
		txService.invokeInTx(() -> {
			jpaDao.getList(ScheduledJob.class).forEach(schedulerService::onBoot);
			return null;
		});
	}

}
