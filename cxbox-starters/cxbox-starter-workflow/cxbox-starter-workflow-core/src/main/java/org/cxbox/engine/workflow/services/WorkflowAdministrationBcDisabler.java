
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

package org.cxbox.engine.workflow.services;

import org.cxbox.WorkflowServiceAssociation;
import org.cxbox.core.crudma.bc.BcIdentifier;
import org.cxbox.core.crudma.bc.BcIdentifiers;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.service.rowmeta.BcDisabler;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.workflow.entity.WorkflowVersion;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor

@Service
public class WorkflowAdministrationBcDisabler extends BcDisabler {

	private final BcRegistry bcRegistry;

	private final JpaDao jpaDao;

	private final BcIdentifiers ignoredBcIdentifiers = BcIdentifiers
			.of(WorkflowServiceAssociation.wfTaskMigration, WorkflowServiceAssociation.wfTemplateMigration);

	@Override
	public Collection<BcIdentifier> getSupportedBc() {
		return bcRegistry.select(bcDescription -> {
			String parentBcName = bcDescription.getParentName();
			while (parentBcName != null) {
				final BcDescription parentBc = bcRegistry.getBcDescription(parentBcName);
				if (WorkflowServiceAssociation.wfVersion.isBc(parentBc)) {
					return true;
				}
				parentBcName = parentBc.getParentName();
			}
			return false;
		}).filter(bcDescription -> !ignoredBcIdentifiers.contains(bcDescription)).collect(Collectors.toList());
	}

	@Override
	public boolean isBcDisabled(final BusinessComponent bc) {
		String wfVersionId = bc.getHierarchy().getId(WorkflowServiceAssociation.wfVersion.getName());
		wfVersionId = Objects.equals(wfVersionId, "null") ? null : wfVersionId;
		if (wfVersionId == null) {
			return true;
		}
		final WorkflowVersion workflowVersion = jpaDao.findById(
				WorkflowVersion.class,
				NumberUtils.createLong(wfVersionId)
		);
		if (workflowVersion != null) {
			return !workflowVersion.isDraft();
		}
		return true;
	}

	@Override
	protected boolean isActionDisabled(final String actionName) {
		return true;
	}

}
