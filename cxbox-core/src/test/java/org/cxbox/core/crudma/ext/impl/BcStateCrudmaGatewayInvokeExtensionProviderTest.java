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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.DataResponseDTO_;
import org.cxbox.api.data.dto.rowmeta.FieldDTO;
import org.cxbox.api.data.dto.rowmeta.FieldsDTO;
import org.cxbox.api.util.Invoker;
import org.cxbox.core.controller.BCFactory;
import org.cxbox.core.crudma.CrudmaActionHolder.CrudmaAction;
import org.cxbox.core.crudma.CrudmaActionHolder.SimpleCrudmaAction;
import org.cxbox.core.crudma.CrudmaActionType;
import org.cxbox.core.crudma.InterimResult;
import org.cxbox.core.crudma.bc.BcHierarchy;
import org.cxbox.core.crudma.bc.BcRegistry;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import org.cxbox.core.crudma.bc.impl.ExtremeBcDescription;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.crudma.state.BcState;
import org.cxbox.core.crudma.state.BcStateAware;
import org.cxbox.core.dto.DrillDownType;
import org.cxbox.core.dto.rowmeta.ActionResultDTO;
import org.cxbox.core.dto.rowmeta.ActionType;
import org.cxbox.core.dto.rowmeta.ActionsDTO;
import org.cxbox.core.dto.rowmeta.MetaDTO;
import org.cxbox.core.dto.rowmeta.PostAction;
import org.cxbox.core.dto.rowmeta.RowMetaDTO;
import org.cxbox.core.external.core.ParentDtoFirstLevelCache;
import org.cxbox.core.service.ResponseFactory;
import org.cxbox.core.service.ResponseService;
import org.cxbox.core.test.util.TestResponseDto;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BcStateCrudmaGatewayInvokeExtensionProviderTest {

	@Mock
	BcRegistry bcRegistry;

	@Mock
	BCFactory bcFactory;

	@Mock
	ResponseFactory respFactory;

	@Mock
	BcStateAware bcStateAware;

	@Mock
	ParentDtoFirstLevelCache parentDtoFirstLevelCache;

	private BcDescription bcDescription;

	private BusinessComponent bc;

	private InterimResult interimResult;

	@InjectMocks
	BcStateCrudmaGatewayInvokeExtensionProvider bcStateCrudmaGatewayInvokeExtensionProvider;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		bcDescription = new ExtremeBcDescription("name", "parent", null, true);
		bc = new BusinessComponent("id", "parentId", bcDescription);
		interimResult = new InterimResult(bc, null, null);
		ResponseService<?, ?> responseService = mock(ResponseService.class);
		when(responseService.onCancel(any())).thenReturn(new ActionResultDTO<>());
		when(respFactory.getService(any())).thenReturn(responseService);
		when(bcRegistry.getBcDescription(anyString())).thenReturn(bcDescription);
		when(bcFactory.getBusinessComponent(any(), any())).thenReturn(bc);
		when(bcStateAware.getState(any())).thenReturn(new BcState(new TestDto(), true, null));
		when(bcStateAware.isPersisted(any())).thenReturn(true);
	}

	private static class TestDto extends DataResponseDTO {

	}

	@Test
	void testExtendInvoker() {
		CrudmaAction crudmaAction = new SimpleCrudmaAction(CrudmaActionType.INVOKE);
		crudmaAction.setBc(bc);
		Invoker<Object, RuntimeException> result = bcStateCrudmaGatewayInvokeExtensionProvider
				.extendInvoker(crudmaAction, () -> true, false);
		Assertions.assertEquals(true, result.invoke());
	}

	@Test
	void testExtendInvokerWithCancel() {
		CrudmaAction crudmaAction = new SimpleCrudmaAction(CrudmaActionType.INVOKE);
		crudmaAction.setName(ActionType.CANCEL_CREATE.getType());
		crudmaAction.setBc(bc);
		Invoker<Object, RuntimeException> result = bcStateCrudmaGatewayInvokeExtensionProvider
				.extendInvoker(crudmaAction, () -> true, true);
		Assertions.assertEquals(ActionResultDTO.class, result.invoke().getClass());
		bcDescription = new InnerBcDescription("name", "parent", null, true);
		bc = new BusinessComponent("id", "parentId", bcDescription);
		crudmaAction.setBc(bc);
		result = bcStateCrudmaGatewayInvokeExtensionProvider.extendInvoker(crudmaAction, () -> true, true);
		Assertions.assertEquals(ActionResultDTO.class, result.invoke().getClass());
	}

	@Test
	void testExtendInvokerWithCreateAction() {
		CrudmaAction crudmaAction = new SimpleCrudmaAction(CrudmaActionType.CREATE);
		crudmaAction.setBc(bc);
		TestResponseDto dto = new TestResponseDto();
		dto.setId("1");
		MetaDTO metaDTO = new MetaDTO(
				new RowMetaDTO(
						new ActionsDTO(),
						new FieldsDTO(),
						null,
						0
				)
		);
		metaDTO.setPostActions(Collections.singletonList(
				PostAction.drillDown(DrillDownType.INNER, "screen/somescreen/view/someview/somebc/1")
		));
		interimResult = new InterimResult(bc, dto, metaDTO);
		Invoker<Object, RuntimeException> result = bcStateCrudmaGatewayInvokeExtensionProvider
				.extendInvoker(crudmaAction, () -> interimResult, true);
		Assertions.assertEquals(InterimResult.class, result.invoke().getClass());
		metaDTO.setPostActions(Collections.emptyList());
		result = bcStateCrudmaGatewayInvokeExtensionProvider.extendInvoker(crudmaAction, () -> interimResult, true);
		Assertions.assertEquals(InterimResult.class, result.invoke().getClass());
	}

	@Test
	void testExtendInvokerWithPreviewAction() {
		CrudmaAction crudmaAction = new SimpleCrudmaAction(CrudmaActionType.PREVIEW);
		when(bcStateAware.isPersisted(any())).thenReturn(false);
		crudmaAction.setBc(bc);
		TestResponseDto dto = new TestResponseDto();
		dto.setId("1");
		MetaDTO metaDTO = new MetaDTO(
				new RowMetaDTO(
						new ActionsDTO(),
						new FieldsDTO(),
						null,
						0
				)
		);
		metaDTO.setPostActions(Collections.emptyList());
		interimResult = new InterimResult(bc, dto, metaDTO);
		Invoker<Object, RuntimeException> result = bcStateCrudmaGatewayInvokeExtensionProvider
				.extendInvoker(crudmaAction, () -> interimResult, true);
		Assertions.assertEquals(InterimResult.class, result.invoke().getClass());
	}

	@Test
	void testExtendInvokerWithMetaAction() {
		CrudmaAction crudmaAction = new SimpleCrudmaAction(CrudmaActionType.META);
		when(bcStateAware.isPersisted(any())).thenReturn(false);
		crudmaAction.setBc(bc);
		FieldsDTO fields = new FieldsDTO();
		FieldDTO fieldDTO = FieldDTO.disabledField(DataResponseDTO_.vstamp.getName());
		fieldDTO.setCurrentValue("1");
		fields.add(fieldDTO);
		MetaDTO metaDTO = new MetaDTO(
				new RowMetaDTO(
						new ActionsDTO(),
						fields,
						null,
						0
				)
		);
		metaDTO.setPostActions(Collections.emptyList());
		Invoker<Object, RuntimeException> result = bcStateCrudmaGatewayInvokeExtensionProvider
				.extendInvoker(crudmaAction, () -> metaDTO, true);
		Assertions.assertEquals(MetaDTO.class, result.invoke().getClass());
	}

	@Test
	void testExtendInvokerWithGetAction() {
		CrudmaAction crudmaAction = new SimpleCrudmaAction(CrudmaActionType.GET);
		when(bcStateAware.isPersisted(any())).thenReturn(false);
		crudmaAction.setBc(bc);
		TestResponseDto dto = new TestResponseDto();
		dto.setId("1");
		Invoker<Object, RuntimeException> result = bcStateCrudmaGatewayInvokeExtensionProvider
				.extendInvoker(crudmaAction, () -> dto, true);
		Assertions.assertEquals(TestResponseDto.class, result.invoke().getClass());
	}

	@Test
	void testExtendInvokerRestoreState() {
		CrudmaAction crudmaAction = new SimpleCrudmaAction(CrudmaActionType.INVOKE);
		crudmaAction.setBc(bc);
		Invoker<Object, RuntimeException> result = bcStateCrudmaGatewayInvokeExtensionProvider
				.extendInvoker(crudmaAction, () -> "ExpectedResult", false);
		Assertions.assertEquals("ExpectedResult", result.invoke());
		bcDescription = new InnerBcDescription("name", "parent", null, true);
		BcHierarchy bcHierarchy = new BcHierarchy(
				"screen", "id", "name", new BcHierarchy(
				"screen", "parent", "parentId", null
		)
		);
		bc = new BusinessComponent("id", "parentId", bcDescription, bcHierarchy);
		crudmaAction.setBc(bc);
		result = bcStateCrudmaGatewayInvokeExtensionProvider
				.extendInvoker(crudmaAction, () -> "ExpectedResult", false);
		Assertions.assertEquals("ExpectedResult", result.invoke());
		bcDescription = new InnerBcDescription("name", "parent", null, true);
		bc = new BusinessComponent("id", "parentId", bcDescription, bcHierarchy);
		crudmaAction.setBc(bc);
		result = bcStateCrudmaGatewayInvokeExtensionProvider
				.extendInvoker(crudmaAction, () -> "ExpectedResult", false);
		Assertions.assertEquals("ExpectedResult", result.invoke());
	}

}
