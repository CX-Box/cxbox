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

package org.cxbox.core.dto.rowmeta;

import static org.cxbox.api.data.dictionary.DictionaryCache.dictionary;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.cxbox.api.data.dictionary.IDictionaryType;
import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.api.data.dictionary.SimpleDictionary;
import org.cxbox.api.data.dto.DataResponseDTO;
import org.cxbox.api.data.dto.hierarhy.grouping.GroupByField;
import org.cxbox.api.data.dto.hierarhy.grouping.Hierarchy;
import org.cxbox.api.data.dto.hierarhy.grouping.HierarchyWithFields;
import org.cxbox.api.data.dto.rowmeta.Icon;
import org.cxbox.api.data.dto.rowmeta.IconCode;
import org.cxbox.constgen.DtoField;

@Slf4j
public class FieldsMeta<T extends DataResponseDTO> extends RowDependentFieldsMeta<T> {

	public FieldsMeta(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	/**
	 * Adds a value to the existing list of filterable values
	 *
	 * @param field widget field with type dictionary
	 * @param dictDTO DTO with dictionary value
	 */
	public final void addConcreteFilterValue(DtoField<? super T, ?> field, SimpleDictionary dictDTO) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> fieldDTO.addFilterValue(dictDTO));
	}

	//DtoField METHODS
	@SafeVarargs
	public final void enableFilter(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
								dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setFilterable(true)));
	}

	/**
	 * @param field dto field
	 * @param type dictionary type
	 * <p>
	 * <br>
	 * Field filter drop-downs (on List widgets header and so on) values sorted by display_order, then by key. display_order can be null
	 * <p>
	 * See dicts.sort and LinkedHashMap lines in {@link org.cxbox.model.core.service.DictionaryCacheImpl.Cache#load()}
	 * <p>
	 * <br>
	 * Attention - sorting rows in List widgets always ignores display_order and is done by lov.key lexicographically!
	 */
	public final void setAllFilterValuesByLovType(DtoField<? super T, ?> field, IDictionaryType type) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearFilterValues();
					fieldDTO.setFilterValues(dictionary().getAll(type));
				});
	}

	/**
	 * @param field dto field
	 * @param type dictionary type
	 * @param comparator filter drop-downs will show values sorted by this comparator
	 * <p>
	 * Attention - sorting rows in List widgets always ignores display_order and is done by lov.key lexicographically!
	 */
	public final void setAllFilterValuesByLovType(
			final DtoField<?, ?> field,
			@NonNull final IDictionaryType type,
			@NonNull final Comparator<SimpleDictionary> comparator) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearFilterValues();
					fieldDTO.setFilterValues(dictionary().getAll(type)
							.stream()
							.filter(Objects::nonNull)
							.sorted(comparator)
							.toList()
					);
				});
	}

	public final void setConcreteFilterValues(DtoField<? super T, ?> field, Collection<SimpleDictionary> dictDtoList) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearFilterValues();
					fieldDTO.setFilterValues(dictDtoList);
				});
	}


	public <T extends DataResponseDTO, E extends Enum> void setEnumFilterValues(
			@NonNull FieldsMeta<T> fieldsMeta,
			@Nullable DtoField<? super T, E> field,
			@NonNull E... values
	) {
		if (field != null) {
			fieldsMeta.setConcreteFilterValues(field, Arrays
					.stream(values)
					.map(en -> new SimpleDictionary(en.name(), serialize(en)))
					.collect(Collectors.toList())
			);
		}
	}

	@SafeVarargs
	public final void setForceActive(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
								dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setForceActive(true)));

	}

	@SafeVarargs
	public final void setEphemeral(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
								dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setEphemeral(true)));
	}

	/**
	 * @deprecated Since 4.0.0-M11
	 * use {@link FieldsMeta#setAllValuesWithIcons(DtoField, IDictionaryType, Map)}
	 */
	@Deprecated(since = "4.0.0-M11")
	public final void setFilterValuesWithIcons(DtoField<? super T, ?> field, IDictionaryType type,
			Map<LOV, IconCode> valueIconMap) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setDictionaryName(type.getName());
					fieldDTO.clearValues();
					valueIconMap
							.forEach((key, value) -> fieldDTO
									.setIconWithValue(type.lookupValue(key), value, true));
				});
	}

	/**
	 * Show icon for fields having "type":"dictionary" in widget.json based on LOV.
	 * Icon will appear in both From, List widgets.
	 * For List widget filtration and rows will get icons - no other method are needed to be called
	 * Icon can depend on parent bc
	 *
	 * @param field dto field
	 * @param type dictionary type
	 * @param valueIconMap <LOV, Icon> LOV to icon mapping
	 */
	public final void setAllValuesWithIcons(
			DtoField<? super T, ?> field,
			IDictionaryType type,
			Map<LOV, Icon> valueIconMap) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.clearAllValues();
					valueIconMap
							.forEach((key, value) ->
									fieldDTO.setIconWithValue(type.lookupValue(key), value)
							);
				});
	}

	/**
	 * Same as {@link FieldsMeta#setAllValuesWithIcons(DtoField, IDictionaryType, Map)} but for Enum base dictionary fields
	 *
	 * @param field dto field
	 * @param valueIconMap <extends Enum, Icon> Enum to icon mapping
	 */
	public final <E extends Enum> void setAllValuesWithIcons(DtoField<? super T, ?> field,
			Map<E, Icon> valueIconMap) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
							fieldDTO.clearAllValues();
							valueIconMap
									.forEach((key, value) ->
											fieldDTO.setIconWithValue(serialize(key), value)
									);
						}
				);
	}


	public final void setFileAccept(DtoField<? super T, ?> field, @NonNull List<String> accept) {
		Optional.ofNullable(field).map(dtoField -> fields.get(dtoField.getName()))
				.ifPresent(fieldDTO -> {
					fieldDTO.setFileAccept(null);
					fieldDTO.setFileAccept(String.join(",", accept));
				});
	}

	/**
	 * @param fields fields to be made <code>sortable</code>. Sort icon will appear in UI, that user can interact with to apply/change sorting order
	 * <ul>
	 *     <li>See additional abilities for sorting  (how to set <code>default sort order</code> and so on) in this java doc
	 *     {@link org.cxbox.core.config.properties.WidgetFieldsIdResolverProperties#sortEnabledDefault}</li>
	 * </ul>
	 */
	@SafeVarargs
	public final void enableSort(DtoField<? super T, ?>... fields) {
		Stream.of(fields).forEach(
				field -> Optional.ofNullable(field).map(
								dtoField -> this.fields.get(dtoField.getName()))
						.ifPresent(fieldDTO -> fieldDTO.setSortable(true)));
	}

	/**
	 * <br>
	 * <br>
	 * This method sets default hierarchy for "GroupingHierarchy" widget, that will always be shown (even, when widget has no data from backend).
	 * Use this method only for hierarchies grouped by SINGLE column
	 * <br>
	 * <br>
	 * Example 1: <strong>explicitly</strong> provided default hierarchy (grouped by single Enum field <strong>document</strong>):
	 * <pre>{@code
	 * fields.defaultGroupingHierarchy(
	 *  MeetingDocumentsDTO_.document,
	 *  lvl -> lvl
	 *    .add(Documents.REFERENCE),
	 *    .add(Documents.POLICY)
	 * );
	 * }</pre>
	 * <br>
	 * Resulting "GroupingHierarchy" widget in UI, when NO data came from backend (e.g. only default hierarchy will be shown):
	 * <pre>{@code
	 *  UI ("default hierarchy")
	 * _________________________
	 * |Document↓   |File      |
	 * _________________________
	 * |Reference(0)|          |
	 * |Policy   (0)|          |
	 * _________________________
	 *            ↑ ↑
	 *       Backend data
	 * _________________________
	 * |Document    |File      |
	 * _________________________
	 * _________________________
	 *
	 * }</pre>
	 * Resulting "GroupingHierarchy" widget in UI, when data came from backend (e.g. default hierarchy merged with backend data will be shown)
	 * <pre>{@code
	 * UI ("default hierarchy" merged with data)
	 * _________________________
	 * |Document↓    |File     |
	 * _________________________
	 * |Reference (0)|         |
	 * |Policy↓   (2)|File1.jpg|
	 * |             |File2.jpg|
	 * |Legal     (1)|File3.jpg|
	 * _________________________
	 *            ↑ ↑
	 *       Backend data
	 * _________________________
	 * |Document     |File     |
	 * _________________________
	 * |Policy       |File1.jpg|
	 * |Policy       |File2.jpg|
	 * |Legal        |File3.jpg|
	 * _________________________
	 * }</pre>
	 * <br>
	 * <br>
	 * Example 2: <strong>dynamically</strong> provided default hierarchy tree (grouped by single Enum field <strong>document</strong>). Can be convenient, when default hierarchy structure is configurable through admin UI, so needed to be loaded from DB/microservice:
	 * <pre>{@code
	 * fields.defaultGroupingHierarchy(
	 *  MeetingDocumentsDTO_.document,
	 *  l -> Arrays.stream(Documents.values()).collect(Hierarchy.toHierarchyWithCfg(
	 *    e -> e,
	 *    (e, cfg) -> cfg.options(Map.of("sdfsdf", "sdzfdsf"))
	 *   )
	 *  )
	 * );
	 * }</pre>
	 * <br>
	 * <br>
	 * Preconditions for both examples:
	 * <pre>{@code
	 * {
	 *  "name": "meetingDocumentsList",
	 *  "title": "",
	 *  "type": "GroupingHierarchy",
	 *  "bc": "meetingDocumentEdit",
	 *  "fields": [
	 *    {
	 *      "title": "Document",
	 *      "key": "document",
	 *      "type": "dictionary"
	 *    },
	 *    {
	 *      "title": "File",
	 *      "key": "file",
	 *      "type": "fileUpload",
	 *      "fileIdKey": "fileId"
	 *    }
	 *  ],
	 *  "options": {
	 *    "groupingHierarchy": {
	 *      "counterMode": "always",
	 *      "fields": ["document"]
	 *    }
	 *  }
	 * }
	 * }</pre>
	 * <pre>{@code
	 * public enum Documents {
	 *  REFERENCE("Reference"),
	 *  POLICY("Policy"),
	 *  LEGAL("Legal");
	 *
	 *  @JsonValue
	 *  private final String value;
	 * }
	 * }</pre>
	 * <pre>{@code
	 * public class MeetingDocumentsDTO extends DataResponseDTO {
	 *  @SearchParameter(name = "document", provider = EnumValueProvider.class)
	 *  private Documents document;
	 *
	 *  @SearchParameter(name = "file", provider = StringValueProvider.class)
	 *  private String file;
	 *
	 *  private String fileId;;
	 * }
	 * }</pre>
	 * see details in <a href="https://doc.cxbox.org/">documentation</a>
	 * <br>
	 * <br>
	 *
	 * @param field1 FIRST field listed in .widget. json -> "options" -> "groupingHierarchy" -> "fields"
	 * @param hierarchyBuilder builder for default hierarchy. See usage example at this java-doc beginning
	 * @param <D> DTO type
	 * @param <E1> DTO field type. Usually one will use field with "type":"input" or "type":"dictionary, so DTO field types will usually be String or Enum
	 */
	public <D extends DataResponseDTO, E1> void defaultGroupingHierarchy(
			@NonNull DtoField<D, E1> field1,
			@NonNull UnaryOperator<Hierarchy<E1, ?>> hierarchyBuilder) {
		defaultGroupingHierarchy(List.of(field1), hierarchyBuilder.apply(new Hierarchy<>()));
	}

	/**
	 * <br>
	 * <br>
	 * This method sets default hierarchy for "GroupingHierarchy" widget, that will always be shown (even, when widget has no data from backend).
	 * Use this method only for hierarchies grouped by TWO columns
	 * <br>
	 * <br>
	 * Example 1: <strong>explicitly</strong> provided default hierarchy tree (grouped by two Enum fields <strong>document</strong> and then <strong>briefing</strong>):
	 * <pre>{@code
	 * fields.defaultGroupingHierarchy(
	 *  MeetingDocumentsDTO_.document,
	 *  MeetingDocumentsDTO_.briefing,
	 *  lvl -> lvl
	 *    .add(
	 *        Documents.REFERENCE,
	 *        lvl2 -> lvl2.
	 *          add(Briefings.FINANCIAL),
	 *          add(Briefings.PROJECT)
	 *        )
	 *     ),
	 *    .add(
	 *        Documents.POLICY
	 *     )
	 *  )
	 * );
	 * }</pre>
	 * <br>
	 * Resulting "GroupingHierarchy" widget in UI, when NO data came from backend (e.g. only default hierarchy will be shown):
	 * <pre>{@code
	 *       UI ("default hierarchy")
	 * _______________________________________
	 * |Document↓    | Briefing↓   |File     |
	 * _______________________________________
	 * |Reference↓(0)| Financial(0)|         |
	 * |             | Project  (0)|         |
	 * |Policy    (0)|             |         |
	 * _______________________________________
	 *                   ↑ ↑
	 *              Backend data
	 * _______________________________________
	 * |Document     | Briefing    |File     |
	 * _______________________________________
	 * ______________________________________|
	 * }</pre>
	 * <br>
	 * Resulting "GroupingHierarchy" widget in UI, when data came from backend (e.g. default hierarchy merged with backend data will be shown)
	 * <pre>{@code
	 * UI ("default hierarchy" merged with data)
	 * _________________________________________
	 * |Document↓    | Briefing↓     |File     |
	 * _________________________________________
	 * |Reference↓(1)| Financial  (0)|         |
	 * |             | Project    (0)|         |
	 * |             | Operational(1)|File1.jpg|
	 * |Policy↓   (3)| Security↓  (2)|File2.jpg|
	 * |             |               |File3.jpg|
	 * |             | Project    (1)|File4.jpg|
	 * |Legal     (1)| Operational(1)|File5.jpg|
	 * _________________________________________
	 *                   ↑ ↑
	 *              Backend data
	 * _________________________________________
	 * |Document     | Briefing      |File     |
	 * _________________________________________
	 * |Reference    | Operational   |File1.jpg|
	 * |Policy       | Security      |File2.jpg|
	 * |Policy       | Security      |File3.jpg|
	 * |Policy       | Project       |File4.jpg|
	 * |Legal        | Operational   |File5.jpg|
	 * _________________________________________
	 * }</pre>
	 * <br>
	 * <br>
	 * Example 2: <strong>dynamically</strong> provided default hierarchy tree (grouped by single Enum field <strong>document</strong>). Can be convenient, when default hierarchy structure is configurable through admin UI, so needed to be loaded from DB/microservice:
	 * <pre>{@code
	 * Map<Documents, Set<Briefings>> external = Map.of(
	 *  Documents.REFERENCE, Set.of(Briefings.FINANCIAL, Briefings.PROJECT),
	 *  Documents.POLICY, new HashSet<>()
	 * );
	 * fields.defaultGroupingHierarchy(
	 *  MeetingDocumentsDTO_.document,
	 *  MeetingDocumentsDTO_.briefing,
	 *  lvl1 -> external.entrySet().stream().collect(Hierarchy.toHierarchy(
	 *    Entry::getKey,
	 *    (doc, lvl2) -> doc.getValue().stream().collect(Hierarchy.toHierarchy(brief -> brief))
	 *   )
	 *  )
	 * );
	 * }</pre>
	 * <br>
	 * <br>
	 * Preconditions for both examples:
	 * <pre>{@code
	 * {
	 *  "name": "meetingDocumentsList",
	 *  "title": "",
	 *  "type": "GroupingHierarchy",
	 *  "bc": "meetingDocumentEdit",
	 *  "fields": [
	 *    {
	 *      "title": "Document",
	 *      "key": "document",
	 *      "type": "dictionary"
	 *    },
	 *    {
	 *      "title": "Briefing",
	 *      "key": "briefing",
	 *      "type": "dictionary"
	 *    },
	 *    {
	 *      "title": "File",
	 *      "key": "file",
	 *      "type": "fileUpload",
	 *      "fileIdKey": "fileId"
	 *    }
	 *  ],
	 *  "options": {
	 *    "groupingHierarchy": {
	 *      "counterMode": "always",
	 *      "fields": ["document", "briefing"]
	 *    }
	 *  }
	 * }
	 * }</pre>
	 * <pre>{@code
	 * public enum Documents {
	 *  REFERENCE("Reference"),
	 *  POLICY("Policy"),
	 *  LEGAL("Legal");
	 *
	 *  @JsonValue
	 *  private final String value;
	 * }
	 * }</pre>
	 * <pre>{@code
	 * public enum Briefings {
	 *  FINANCIAL("Financial"),
	 *  PROJECT("Project"),
	 *  SECURITY("Security"),
	 *  OPERATIONAL("Operational");
	 *
	 *  @JsonValue
	 *  private final String value;
	 * }
	 * }</pre>
	 * <pre>{@code
	 * public class MeetingDocumentsDTO extends DataResponseDTO {
	 *  @SearchParameter(name = "document", provider = EnumValueProvider.class)
	 *  private Documents document;
	 *
	 *  @SearchParameter(name = "briefing", provider = EnumValueProvider.class)
	 *  private Briefings briefing;
	 *
	 *  @SearchParameter(name = "file", provider = StringValueProvider.class)
	 *  private String file;
	 *
	 *  private String fileId;;
	 * }
	 * }</pre>
	 * <br>
	 *
	 * @param field1 FIRST field listed in .widget. json -> "options" -> "groupingHierarchy" -> "fields"
	 * @param field2 SECOND field listed in .widget. json -> "options" -> "groupingHierarchy" -> "fields"
	 * @param hierarchyBuilder builder for default hierarchy. See usage example at this java-doc beginning
	 * @param <D> DTO type
	 * @param <E1> DTO field type. Usually one will use field with "type":"input" or "type":"dictionary, so DTO field types will usually be String or Enum
	 */
	public <D extends DataResponseDTO, E1, E2> void defaultGroupingHierarchy(
			@NonNull DtoField<D, E1> field1,
			@NonNull DtoField<D, E2> field2,
			@NonNull UnaryOperator<Hierarchy<E1, Hierarchy<E2, ?>>> hierarchyBuilder) {
		defaultGroupingHierarchy(List.of(field1, field2), hierarchyBuilder.apply(new Hierarchy<>()));
	}


	/**
	 * <br>
	 * <br>
	 * This method sets default hierarchy for "GroupingHierarchy" widget, that will always be shown (even, when widget has no data from backend).
	 * Use this method only for hierarchies grouped by TREE columns
	 * <br>
	 * <br>
	 * See usage example here {@link FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)}
	 * <br>
	 * <br>
	 *
	 * @param field1 FIRST  field listed in .widget.json -> "options" -> "groupingHierarchy" -> "fields"
	 * @param field2 SECOND field listed in .widget.json -> "options" -> "groupingHierarchy" -> "fields"
	 * @param field3 THIRD  field listed in .widget.json -> "options" -> "groupingHierarchy" -> "fields"
	 * @param hierarchyBuilder builder for default hierarchy. See usage example at this java-doc beginning
	 * @param <D> DTO type
	 * @param <E1> DTO field type. Usually one will use field with "type":"input" or "type":"dictionary, so DTO field types will usually be String or Enum
	 */
	public <D extends DataResponseDTO, E1, E2, E3> void defaultGroupingHierarchy(
			@NonNull DtoField<D, E1> field1,
			@NonNull DtoField<D, E2> field2,
			@NonNull DtoField<D, E3> field3,
			@NonNull UnaryOperator<Hierarchy<E1, Hierarchy<E2, Hierarchy<E3, ?>>>> hierarchyBuilder) {
		defaultGroupingHierarchy(List.of(field1, field2, field3), hierarchyBuilder.apply(new Hierarchy<>()));
	}

	/**
	 * ---------------------------------------------------------------------------------------------------------------
	 * <br>
	 * This method sets default hierarchy for "GroupingHierarchy" widget, that will always be shown (even, when widget has no data from backend).
	 * Use this method only for hierarchies grouped by FOUR columns
	 * <br>
	 * <br>
	 * See usage example here {@link FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)}
	 * <br>
	 * <br>
	 *
	 * @param field1 FIRST  field listed in .widget.json -> "options" -> "groupingHierarchy" -> "fields"
	 * @param field2 SECOND field listed in .widget.json -> "options" -> "groupingHierarchy" -> "fields"
	 * @param field3 THIRD  field listed in .widget.json -> "options" -> "groupingHierarchy" -> "fields"
	 * @param field4 FOURTH  field listed in .widget.json -> "options" -> "groupingHierarchy" -> "fields"
	 * @param hierarchyBuilder builder for default hierarchy. See usage example at this java-doc beginning
	 * @param <D> DTO type
	 * @param <E1> DTO field type. Usually one will use field with "type":"input" or "type":"dictionary, so DTO field types will usually be String or Enum
	 */
	public <D extends DataResponseDTO, E1, E2, E3, E4> void defaultGroupingHierarchy(
			@NonNull DtoField<D, E1> field1,
			@NonNull DtoField<D, E2> field2,
			@NonNull DtoField<D, E3> field3,
			@NonNull DtoField<D, E4> field4,
			@NonNull UnaryOperator<Hierarchy<E1, Hierarchy<E2, Hierarchy<E3, Hierarchy<E4, ?>>>>> hierarchyBuilder) {
		defaultGroupingHierarchy(List.of(field1, field2, field3, field4), hierarchyBuilder.apply(new Hierarchy<>()));
	}

	/**
	 * <br>
	 * <br>
	 * Internal API.
	 * <br>
	 * <br>
	 * This method sets default hierarchy for "GroupingHierarchy" widget, that will always be shown (even, when widget has no data from backend).
	 * This method is designed to support hierarchies grouped by more, then FOUR columns, but is not strongly typed, so is marked as internal for other cases.
	 * <br>
	 * <br>
	 * Please, do not use this method directly. Use one of existing STRONGLY TYPED methods:
	 * <ul>
	 * <li> {@link FieldsMeta#defaultGroupingHierarchy(DtoField, UnaryOperator)} (see examples here)</li>
	 * <li> {@link FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)} (see examples here) </li>
	 * <li> {@link FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, DtoField, UnaryOperator)} </li>
	 * <li> {@link FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, DtoField, DtoField, UnaryOperator)} </li>
	 * </ul>
	 * <br>
	 * or create own analog if more, then FOUR hierarchy levels are needed
	 *
	 * @param groupByFields for widget with "type": "GroupingHierarchy" exactly equal to fields listed in .widget.json -> "options" -> "groupingHierarchy" -> "fields". Fields must be listed in same sequence
	 * @param hierarchy hierarchy structure.
	 * This structure will be shown even when widget has no data. If data is present - hierarchy parts that are not present in data will be shown too
	 * @param <D> - DTO
	 */
	public <D extends DataResponseDTO> void defaultGroupingHierarchy(@NonNull List<DtoField<D, ?>> groupByFields,
			@NonNull Hierarchy<?, ?> hierarchy) {
		var field = groupByFields.stream()
				.filter(Objects::nonNull)
				.map(e -> GroupByField.builder()
						.name(e.getName())
						.options(null)
						.build())
				.toList();
		Optional.ofNullable(fields.get(field.get(0).getName()))
				.ifPresent(fieldDTO -> fieldDTO.setDefaultGroupingHierarchy(new HierarchyWithFields(
						field,
						hierarchy.getSubTrees()
				)));
	}

}
