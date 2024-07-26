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

package org.cxbox.sqlbc.crudma;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.NoResultException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.ExtendedDtoFieldLevelSecurityService;
import org.cxbox.api.config.CxboxBeanProperties;
import org.cxbox.api.data.ResultPage;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.api.data.dto.rowmeta.FieldsDTO;
import org.cxbox.api.util.i18n.ErrorMessageSource;
import org.cxbox.core.controller.param.DateStep;
import org.cxbox.core.controller.param.QueryParameters;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.impl.AbstractCrudmaService;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionsDTO;
import org.cxbox.core.dto.rowmeta.EngineFieldsMeta;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.dto.rowmeta.RowMetaDTO;
import org.cxbox.core.dto.rowmeta.SQLMetaDTO;
import org.cxbox.core.exception.BusinessException;
import org.cxbox.core.service.ResponseFactory;
import org.cxbox.core.service.action.ActionDescription;
import org.cxbox.core.service.action.Actions;
import org.cxbox.core.service.action.ActionsBuilder;
import org.cxbox.core.service.linkedlov.LinkedDictionaryService;
import org.cxbox.core.util.DateTimeUtil;
import org.cxbox.core.util.session.SessionService;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.sqlbc.dao.SqlBcQuery;
import org.cxbox.sqlbc.dao.SqlComponentDao;
import org.cxbox.sqlbc.dao.binds.SqlNamedParameterQueryBinder;
import org.cxbox.sqlbc.dto.SqlBcEditFieldDTO;
import org.cxbox.sqlbc.dto.SqlBcEditFieldDTO_;
import org.cxbox.sqlbc.entity.SqlBcEditField;
import org.cxbox.sqlbc.entity.SqlBcEditField_;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SqlCrudmaService extends AbstractCrudmaService {

	private final SqlComponentDao sqlComponentDao;

	private final ResponseFactory respFactory;

	private final JpaDao jpaDao;

	private final Optional<List<SqlBcAction>> sqlBcActions;

	private final Optional<LinkedDictionaryService> linkedDictionaryService;

	private final Optional<ExtendedDtoFieldLevelSecurityService> extendedDtoFieldLevelSecurityService;

	private final SessionService sessionService;

	private final SqlNamedParameterQueryBinder sqlNamedParameterQueryBinder;

	@Qualifier(CxboxBeanProperties.OBJECT_MAPPER)
	private final ObjectMapper objectMapper;

	@Qualifier("primaryDatabase")
	private final Database primaryDatabase;

	@Override
	public SqlComponentObject get(BusinessComponent bc) {
		try {
			return sqlComponentDao.getOne(bc, bc.getParameters());
		} catch (NoResultException ex) {
			throw new BusinessException().addPopup(ex.getMessage());
		}
	}

	@Override
	public ResultPage<SqlComponentObject> getAll(BusinessComponent bc) {
		ResultPage<SqlComponentObject> page = sqlComponentDao.getPage(bc, bc.getParameters());
		String fieldName = ((SqlBcDescription) bc.getDescription()).getReportDateField();
		checkPivotFilters(page, bc.getParameters(), fieldName);
		return page;
	}

	@Override
	public ActionResultDTO update(BusinessComponent bc, Map<String, Object> data) {

		SqlBcEditFieldDTO dto = (SqlBcEditFieldDTO) respFactory.getDTOFromMap(data, SqlBcEditFieldDTO.class, bc);

		SqlBcDescription sqlBcDesc = bc.getDescription();

		SqlBcEditField sqlBcEditField = jpaDao.getList(
				SqlBcEditField.class,
				(root, query, cb) ->
						cb.and(
								cb.equal(root.get(SqlBcEditField_.parentId), bc.getId()),
								cb.equal(root.get(SqlBcEditField_.bcName), sqlBcDesc.getName())
						)
		).stream().findFirst().orElseGet(() -> {
			SqlBcEditField result = new SqlBcEditField();
			result.setParentId(bc.getId());
			result.setBcName(sqlBcDesc.getName());
			return result;
		});

		if (dto.hasChangedFields()) {
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_string1)) {
				sqlBcEditField.setEditString1(dto.getEdit_string1());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_string2)) {
				sqlBcEditField.setEditString2(dto.getEdit_string2());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_string3)) {
				sqlBcEditField.setEditString3(dto.getEdit_string3());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_string4)) {
				sqlBcEditField.setEditString4(dto.getEdit_string4());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_string5)) {
				sqlBcEditField.setEditString5(dto.getEdit_string5());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_string6)) {
				sqlBcEditField.setEditString6(dto.getEdit_string6());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_string7)) {
				sqlBcEditField.setEditString7(dto.getEdit_string7());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_string8)) {
				sqlBcEditField.setEditString8(dto.getEdit_string8());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_string9)) {
				sqlBcEditField.setEditString9(dto.getEdit_string9());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_string10)) {
				sqlBcEditField.setEditString10(dto.getEdit_string10());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_number1)) {
				sqlBcEditField.setEditNumber1(dto.getEdit_number1().intValue());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_number2)) {
				sqlBcEditField.setEditNumber2(dto.getEdit_number2().intValue());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_number3)) {
				sqlBcEditField.setEditNumber3(dto.getEdit_number3().intValue());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_number4)) {
				sqlBcEditField.setEditNumber4(dto.getEdit_number4().intValue());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_number5)) {
				sqlBcEditField.setEditNumber5(dto.getEdit_number5().intValue());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_date1)) {
				sqlBcEditField.setEditDate1(dto.getEdit_date1());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_date2)) {
				sqlBcEditField.setEditDate2(dto.getEdit_date2());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_date3)) {
				sqlBcEditField.setEditDate3(dto.getEdit_date3());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_date4)) {
				sqlBcEditField.setEditDate4(dto.getEdit_date4());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_date5)) {
				sqlBcEditField.setEditDate5(dto.getEdit_date5());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_lov1)) {
				sqlBcEditField.setEditLov1(dto.getEdit_lov1());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_lov2)) {
				sqlBcEditField.setEditLov2(dto.getEdit_lov2());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_lov3)) {
				sqlBcEditField.setEditLov3(dto.getEdit_lov3());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_lov4)) {
				sqlBcEditField.setEditLov4(dto.getEdit_lov4());
			}
			if (dto.isFieldChanged(SqlBcEditFieldDTO_.edit_lov5)) {
				sqlBcEditField.setEditLov5(dto.getEdit_lov5());
			}
		}

		jpaDao.save(sqlBcEditField);

		SqlBcEditFieldDTO sqlBcEditFieldDTO = new SqlBcEditFieldDTO(sqlBcEditField);
		sqlBcEditFieldDTO.setId(bc.getId());

		return new ActionResultDTO<>(sqlBcEditFieldDTO);
	}

	private void checkPivotFilters(final ResultPage<SqlComponentObject> page, final QueryParameters queryParameters,
			final String fieldName) {
		LocalDateTime dateTo = queryParameters.getDateTo();
		LocalDateTime dateFrom = queryParameters.getDateFrom();
		DateStep dateStep = queryParameters.getDateStep();
		if (fieldName != null && dateFrom != null && dateTo != null) {
			Month monthFrom = dateFrom.getMonth();
			List<SqlComponentObject> result = page.getResult();
			result.removeIf(sqlComponent -> {
				LocalDateTime componentDate = getDate(sqlComponent, fieldName);
				int dayOfMonth = componentDate.getDayOfMonth();
				Month month = componentDate.getMonth();
				Month firstMonthOfQuarter = month.firstMonthOfQuarter();
				boolean isFirstMonthOfQuarter = firstMonthOfQuarter.equals(month);
				boolean isNotInPeriod = componentDate.isAfter(dateTo) || componentDate.isBefore(dateFrom);
				boolean isNotInPeriodYear = componentDate.isAfter(dateTo) || componentDate.isBefore(dateFrom.withDayOfMonth(1));
				switch (dateStep) {
					case YEAR:
						return isNotInPeriodYear || dayOfMonth != 1 || !month.equals(monthFrom);
					case QUARTER:
						return isNotInPeriod || dayOfMonth != 1 || !isFirstMonthOfQuarter;
					case MONTH:
						return isNotInPeriod || dayOfMonth != 1;
					case DAY:
					default:
						return isNotInPeriod;
				}
			});
		}
	}

	private LocalDateTime getDate(final SqlComponentObject sqlComponent, final String fieldName) {
		return DateTimeUtil.toLocalDateTime((Date) sqlComponent.get(fieldName));
	}

	private SQLMetaDTO buildMeta(BusinessComponent bc, List<FieldDTO> fields, ActionsDTO actions) {
		SQLMetaDTO metaDTO = new SQLMetaDTO(new RowMetaDTO(actions, FieldsDTO.of(fields)));
		if (bc.getParameters().isDebug()) {
			setDebugParameters(bc, metaDTO);
		}
		return metaDTO;
	}


	@Override
	public SQLMetaDTO getMeta(BusinessComponent bc) {
		final Set<String> fieldsForCurrentScreen = extendedDtoFieldLevelSecurityService.map(s -> s.getBcFieldsForCurrentScreen(
				bc)).orElse(null);
		final List<FieldDTO> fields = ((SqlBcDescription) bc.getDescription()).getFields().stream()
				.filter(field -> fieldsForCurrentScreen != null && fieldsForCurrentScreen.contains(field.getFieldName()))
				.map(field -> (field.getEditable() && isActionSaveAvailable(bc))
						? FieldDTO.enabledField(field.getFieldName())
						: FieldDTO.disabledFilterableField(field.getFieldName())
				)
				.collect(Collectors.toList());
		EngineFieldsMeta meta = new EngineFieldsMeta(objectMapper);
		fields.forEach(meta::add);
		linkedDictionaryService.ifPresent(
				linkedDictSrvc -> linkedDictSrvc.fillRowMetaWithLinkedDictionaries(meta, bc,
						fields.stream()
								.map(FieldDTO::getKey)
								.collect(Collectors.toSet()),
						false
				)
		);
		return buildMeta(bc, fields, getActions().toDto(bc));
	}

	@Override
	public MetaDTO getMetaEmpty(BusinessComponent bc) {
		return buildMeta(bc, Collections.emptyList(), getActions().toDto(bc));
	}

	@Override
	public long count(BusinessComponent bc) {
		return sqlComponentDao.count(bc, bc.getParameters());
	}

	@Override
	public ActionResultDTO invokeAction(BusinessComponent bc,
			String actionName,
			Map<String, Object> data) {
		final SqlComponentObject dto = getAll(bc).getResult()
				.stream()
				.filter(object -> Objects.equals(object.getId(), bc.getId()))
				.findFirst().orElse(null);

		ActionDescription<SqlComponentObject> action = getActions().getAction(actionName);
		if (action == null || !action.isAvailable(bc)) {
			throw new BusinessException().addPopup(ErrorMessageSource.errorMessage("error.action_unavailable", actionName));
		}
		return action.invoke(bc, dto);
	}

	private Actions<SqlComponentObject> getActions() {
		ActionsBuilder<SqlComponentObject> builder = Actions.builder();
		sqlBcActions.ifPresent(sqlBcActions -> {
			for (final SqlBcAction action : sqlBcActions) {
				builder.action(action.getKey(), action.getText())
						.available(action::isAvailable).invoker(action::invoke).add();
			}
		});
		return builder.save().available(this::isActionSaveAvailable).add().build();
	}

	private boolean isActionSaveAvailable(BusinessComponent bc) {
		return bc.<SqlBcDescription>getDescription().isEditable();
	}

	private void setDebugParameters(BusinessComponent bc, SQLMetaDTO metaDTO) {
		SqlBcDescription description = bc.getDescription();
		metaDTO.setQuery(description.getQuery());
		try {
			SqlBcQuery query = SqlBcQuery.build(
					sessionService,
					description,
					bc.getId(),
					bc.getParentId(),
					bc.getParameters(),
					primaryDatabase
			);
			String preparedQuery = sqlNamedParameterQueryBinder.bindVariables(
					query.pageQuery(),
					query.parameterSource()
			);
			metaDTO.setPreparedQuery(preparedQuery);
		} catch (Exception exc) {
			log.error("Exception while binding parameters to sql", exc);
		}
	}


}
