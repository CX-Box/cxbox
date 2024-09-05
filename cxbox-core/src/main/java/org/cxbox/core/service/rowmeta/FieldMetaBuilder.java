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

package org.cxbox.core.service.rowmeta;

import java.util.Collection;
import java.util.List;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.constgen.DtoField;
import org.cxbox.core.crudma.bc.BusinessComponent;
import org.cxbox.core.crudma.bc.impl.ExtremeBcDescription;
import org.cxbox.core.crudma.bc.impl.InnerBcDescription;
import org.cxbox.core.dto.rowmeta.FieldsMeta;
import org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta;
import org.cxbox.core.service.action.DrillDownTypeSpecifier;


public abstract class FieldMetaBuilder<T extends DataResponseDTO> {

	/**
	 * <ui>
	 * <p>
	 * 	This method configures form/list fields with UI changes:
	 * 	</p>
	 * <li> Enabling field editing  - the field becomes editable (editing is available in the form and in the list by double-clicking on the field) {@link org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta#setEnabled(DtoField[])} #}</li>
	 * <li> Requiring the field - the field becomes mandatory (if the form field is not filled in, an error message will appear) {@link org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta#setRequired(DtoField[])} </li>
	 * <li> Adds a drop-down list to the Radio/Dictionary field of the form with values from the enum that are suitable only for enum fields {@link org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta#setEnumValues(DtoField, Enum[])}</li>
	 * <li> Adds multiselect dropdown to the form field with custom values {@link org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta#setDictionaryTypeWithCustomValues(DtoField, String...)} )}</li>
	 * <li> Setting up drilldown - adds link to the field {@link org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta#setDrilldown(DtoField, DrillDownTypeSpecifier, String)} </li>
	 * <li> Setting hidden - makes fields hidden (the field on the form becomes hidden) {@link org.cxbox.core.dto.rowmeta.RowDependentFieldsMeta#setHidden(DtoField[])}
	 * <li> e.t.c</li>
	 *
	 * <p>
	 * You can specify field configuration conditions within the method:
	 * </p>
	 * <p>Example:</p>
	 * <pre>{@code
	 * if (fields.get(ExampleDTO_.id).getCurrentValue() != null) {
	 *     fields.setEnabled(ExampleDTO_.example);
	 * }
	 * }</pre>
	 * </ui>
	 */
	public void buildRowDependentMeta(RowDependentFieldsMeta<T> fields, BusinessComponent bc) {
		if (bc.getDescription() instanceof InnerBcDescription) {
			buildRowDependentMeta(fields, bc.getDescription(), bc.getIdAsLong(), bc.getParentIdAsLong());
		} else if (bc.getDescription() instanceof ExtremeBcDescription) {
			buildExtremeRowDependentMeta(fields, bc.getDescription(), bc.getIdAsLong(), bc.getParentIdAsLong());
		}
	}

	public void buildIndependentMeta(FieldsMeta<T> fields, BusinessComponent bc) {
		buildIndependentMeta(fields, bc.getDescription(), bc.getParentIdAsLong());
	}

	public abstract void buildRowDependentMeta(RowDependentFieldsMeta<T> fields, InnerBcDescription bcDescription,
			Long id, Long parentId);

	public void buildExtremeRowDependentMeta(RowDependentFieldsMeta<T> fields, ExtremeBcDescription bcDescription,
			Long id, Long parentId) {
	}

	/**
	 * <ui>
	 * <p>
	 * The method configures list/form fields with UI changes:</p>
	 * <li> <code>Filterable</code> - adding column sorting, available only for lists (icon appear) {@link org.cxbox.core.dto.rowmeta.FieldsMeta#enableFilter(DtoField[])}</li>
	 * <li> <code>Sortable</code> - adding column filtering, available only for lists (icon appear) {@link org.cxbox.core.dto.rowmeta.FieldsMeta#enableSort(DtoField[])}</li>
	 * <li> Filtering by enum values - adding filtering of columns with enum values(multiselect dropdown appear) {@link org.cxbox.core.dto.rowmeta.FieldsMeta#setEnumFilterValues(FieldsMeta, DtoField, Enum[])}</li>
	 * <li> Filtering by custom values - adding filtering of columns with custom values(multiselect dropdown appear) {@link org.cxbox.core.dto.rowmeta.FieldsMeta#setDictionaryTypeWithCustomValues(DtoField, String...)}</li>
	 * <li> Specify acceptable formats when uploading files (when adding files, only files of the selected format will be available for addition) {@link org.cxbox.core.dto.rowmeta.FieldsMeta#setFileAccept(DtoField, List)}</li>
	 * <li> Filtering by values obtained from the field - adding filtering of columns with values obtained from the field(multiselect dropdown appear) {@link org.cxbox.core.dto.rowmeta.FieldsMeta#setConcreteFilterValues(DtoField, Collection)}</li>
	 * <li> e.t.c</li>
	 * </ui>
	 */
	public abstract void buildIndependentMeta(FieldsMeta<T> fields, InnerBcDescription bcDescription, Long parentId);

}
