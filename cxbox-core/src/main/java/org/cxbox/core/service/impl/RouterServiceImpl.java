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

package org.cxbox.core.service.impl;

import static java.util.stream.Collectors.groupingBy;

import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.dao.BaseDAO;
import org.cxbox.core.exception.EntityNotFoundException;
import org.cxbox.core.service.EntityUrlBuilder;
import org.cxbox.core.service.RouterService;
import org.cxbox.model.core.dao.util.JpaUtils;
import org.cxbox.model.core.entity.BaseEntity;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@Service(RouterService.SERVICE_NAME)
public class RouterServiceImpl implements RouterService {

	private final Map<Class, List<EntityUrlBuilder>> urlBuilders;

	@Autowired
	private BaseDAO baseDAO;

	@Autowired
	private BcRegistry bcRegistry;

	public RouterServiceImpl(Optional<List<EntityUrlBuilder>> builders) {
		urlBuilders = builders.<Map<Class, List<EntityUrlBuilder>>>map(list ->
				list.stream().collect(groupingBy(
						EntityUrlBuilder::getEntityType
				))
		).orElse(Collections.emptyMap());
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getLocation(String type, Long id, String bcName) {
		BaseEntity entity = baseDAO.findById(type, id);
		if (entity == null) {
			throw new EntityNotFoundException(type, id);
		}
		Optional<BcDescription> bcDescription = Optional.ofNullable(bcName).map(bcRegistry::getBcDescription);
		return Optional.ofNullable(urlBuilders.get(JpaUtils.unproxiedClass(entity)))
				.map(list -> list.stream()
						.map(builder -> builder.buildUrl(entity, bcDescription))
						.filter(Objects::nonNull)
						.findFirst()
						.orElse(null)
				).orElseThrow(UnsupportedOperationException::new);
	}


}
