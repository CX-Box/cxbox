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

package org.cxbox.source.services.crudma;


import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dictionary.DictionaryCache;
import org.cxbox.api.data.dictionary.IDictionaryType;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.AbstractCrudmaService;
import org.cxbox.core.dto.LovUtils;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.util.JsonUtils;
import org.cxbox.core.util.ListPaging;
import org.cxbox.engine.workflow.WorkflowSettings;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.model.workflow.entity.TaskField;
import org.cxbox.source.dto.DmnTaskFieldsDto;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DmnTaskFieldsCrudmaService extends AbstractCrudmaService {

	private final Map<Class<?>, String> TYPES = ImmutableMap.<Class<?>, String>builder()
			.put(String.class, "string")
			.put(Boolean.class, "boolean")
			.put(Boolean.TYPE, "boolean")
			.put(Integer.class, "integer")
			.put(Integer.TYPE, "integer")
			.put(Long.class, "long")
			.put(Long.TYPE, "long")
			.put(Double.class, "double")
			.put(Double.TYPE, "double")
			.put(BigDecimal.class, "double")
			.put(LocalDateTime.class, "date")
			.build();

	private final List<DmnTaskFieldsDto> TASK_FIELDS;

	private final List<FieldDTO> FIELD_DTO_LIST = ImmutableList.<FieldDTO>builder()
			.add(FieldDTO.disabledFilterableField("id"))
			.add(FieldDTO.disabledFilterableField("title"))
			.add(FieldDTO.disabledFilterableField("key"))
			.add(FieldDTO.disabledFilterableField("type"))
			.add(FieldDTO.disabledField("values"))
			.build();

	public DmnTaskFieldsCrudmaService(
			final WorkflowSettings<?> workflowSettings,
			final DictionaryCache dictionaryCache,
			final JpaDao jpaDao) {
		this.TASK_FIELDS = buildTaskFields(workflowSettings, dictionaryCache, jpaDao);
	}

	@Override
	public ResultPage<? extends DataResponseDTO> getAll(final BusinessComponent bc) {
		return ListPaging.getResultPage(TASK_FIELDS, bc.getParameters());
	}

	@Override
	public long count(final BusinessComponent bc) {
		return TASK_FIELDS.size();
	}

	@Override
	public MetaDTO getMeta(final BusinessComponent bc) {
		return buildMeta(FIELD_DTO_LIST);
	}

	@Override
	public MetaDTO getMetaEmpty(BusinessComponent bc) {
		return buildMeta(Collections.emptyList());
	}

	private List<DmnTaskFieldsDto> buildTaskFields(
			final WorkflowSettings<?> workflowSettings,
			final DictionaryCache dictionaryCache,
			final JpaDao jpaDao) {
		final List<DmnTaskFieldsDto> taskFields = new ArrayList<>();
		for (final TaskField taskField : jpaDao.getList(TaskField.class)) {
			final Field field = FieldUtils.getField(workflowSettings.getDtoClass(), taskField.getKey(), true);

			if (field == null) {
				log.error("No field with name " + taskField.getKey() + " in class");
				continue;
			}

			try {
				taskFields.add(new DmnTaskFieldsDto(
						String.valueOf(taskFields.size()),
						taskField.getTitle(),
						taskField.getKey(),
						getDmnType(field.getType()),
						getValues(dictionaryCache, field)
				));
			} catch (Exception e) {
				log.warn(e.getLocalizedMessage(), e);
			}
		}
		return ImmutableList.copyOf(taskFields);
	}

	private String getDmnType(final Class<?> classType) {
		final String dmnType = TYPES.get(classType);
		if (dmnType == null) {
			throw new IllegalArgumentException("Поддержка типа " + classType.getName() + " не реализована DMN движком.");
		}
		return dmnType;
	}

	private String getValues(final DictionaryCache dictionaryCache, final Field field) {
		IDictionaryType dictionaryType = LovUtils.getType(field);
		if (dictionaryType == null) {
			return null;
		}
		return JsonUtils.writeValue(
				dictionaryCache.getAll(dictionaryType).stream()
						.map(SimpleDictionary::getValue)
						.toArray(String[]::new)
		);
	}

}
