/*
 * © OOO "SI IKS LAB", 2022-2024
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

package org.cxbox.api.data.dto.hierarhy.grouping;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.cxbox.api.data.dto.hierarhy.grouping.Config.Cfg;
import org.cxbox.constgen.DtoField;

@Getter
@NoArgsConstructor
public class Hierarchy<T, G extends Hierarchy<?, ?>> {

	private final Set<SubTree<T, ?>> subTrees = new HashSet<>();

	/**
	 * <br>
	 * <br>
	 * Add leaf node
	 * <br>
	 * See usage Example 1 here {@link  org.cxbox.core.dto.rowmeta.FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)}
	 * <br>
	 *
	 * @param value DTO field value
	 * @return this
	 */
	public Hierarchy<T, G> add(@NonNull T value) {
		subTrees.add(new SubTree<>(value, null, null, null));
		return this;
	}

	/**
	 * <br>
	 * <br>
	 * Add node with children
	 * <br>
	 * See usage Example 1 here {@link  org.cxbox.core.dto.rowmeta.FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)}
	 * <br>
	 *
	 * @param value DTO field value
	 * @return this
	 */
	public Hierarchy<T, G> add(@NonNull T value, @NonNull UnaryOperator<G> childHierarchy) {
		var hb = (G) (new Hierarchy<>());
		subTrees.add(new SubTree<>(value, childHierarchy.apply(hb).getSubTrees(), null, null));
		return this;
	}

	/**
	 * <br>
	 * <br>
	 * Add leaf node with configurable params
	 * <br>
	 * See usage Example 1 here {@link  org.cxbox.core.dto.rowmeta.FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)}
	 * <br>
	 *
	 * @param value DTO field value
	 * @return this
	 */
	public Hierarchy<T, G> addWithCfg(@NonNull T value, @NonNull UnaryOperator<Cfg> cfg) {
		Config config = cfg.apply(new Cfg()).build();
		subTrees.add(new SubTree<>(value, null, config.getOptions(), config.getDefaultExpanded()));
		return this;
	}

	/**
	 * <br>
	 * <br>
	 * Add node with children and configurable options
	 * <br>
	 * See usage in Example 2 here {@link  org.cxbox.core.dto.rowmeta.FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)}
	 * <br>
	 *
	 * @param value DTO field value
	 * @return this
	 */
	public Hierarchy<T, G> addWithCfg(@NonNull T value, @NonNull UnaryOperator<Cfg> cfg,
			@NonNull UnaryOperator<G> childHierarchy) {
		var hb = (G) (new Hierarchy<>());
		Config config = cfg.apply(new Cfg()).build();
		subTrees.add(new SubTree<>(
				value,
				childHierarchy.apply(hb).getSubTrees(),
				config.getOptions(),
				config.getDefaultExpanded()
		));
		return this;
	}

	/**
	 * <br>
	 * <br>
	 * Collect to 1 level hierarchy. Without children
	 * <br>
	 * See usage in Example 2 here {@link  org.cxbox.core.dto.rowmeta.FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)}
	 * <br>
	 *
	 * @param valueMapper external value to hierarchy value mapper
	 * @return this
	 */
	public static <E, T, G extends Hierarchy<?, ?>> Collector<E, Hierarchy<T, G>, Hierarchy<T, G>> toHierarchy(
			@NonNull Function<E, T> valueMapper) {
		return toHierarchyWithCfg(valueMapper, (e, cfg) -> cfg, (e, lvl) -> lvl);
	}

	/**
	 * <br>
	 * <br>
	 * Collect to 1 level hierarchy with configurable params. Without children
	 * <br>
	 * See usage in Example 2 here {@link  org.cxbox.core.dto.rowmeta.FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)}
	 * <br>
	 *
	 * @param valueMapper external value to hierarchy value mapper
	 * @param optionsMapper external value to params mapper
	 * @return this
	 */
	public static <E, T, G extends Hierarchy<?, ?>> Collector<E, Hierarchy<T, G>, Hierarchy<T, G>> toHierarchyWithCfg(
			@NonNull Function<E, T> valueMapper,
			@NonNull BiFunction<E, Cfg, Cfg> optionsMapper) {
		return toHierarchyWithCfg(valueMapper, optionsMapper, (e, lvl) -> lvl);
	}

	/**
	 * <br>
	 * <br>
	 * Collect to hierarchy with children
	 * <br>
	 * See usage in Example 2 here {@link  org.cxbox.core.dto.rowmeta.FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)}
	 * <br>
	 *
	 * @param valueMapper external value to hierarchy value mapper
	 * @param subTreeMapper external value to children mapper
	 * @return this
	 */
	public static <E, T, G extends Hierarchy<?, ?>> Collector<E, Hierarchy<T, G>, Hierarchy<T, G>> toHierarchy(
			@NonNull Function<E, T> valueMapper,
			@NonNull BiFunction<E, G, G> subTreeMapper) {
		return toHierarchyWithCfg(valueMapper, (e, cfg) -> cfg, subTreeMapper);
	}

	/**
	 * <br>
	 * <br>
	 * Collect to hierarchy with children and with params
	 * <br>
	 * See usage in Example 2 here {@link  org.cxbox.core.dto.rowmeta.FieldsMeta#defaultGroupingHierarchy(DtoField, DtoField, UnaryOperator)}
	 * <br>
	 *
	 * @param valueMapper external value to hierarchy value mapper
	 * @param subTreeMapper external value to children mapper
	 * @return this
	 */
	public static <E, T, G extends Hierarchy<?, ?>> Collector<E, Hierarchy<T, G>, Hierarchy<T, G>> toHierarchyWithCfg(
			@NonNull Function<E, T> valueMapper,
			@NonNull BiFunction<E, Cfg, Cfg> optionsMapper,
			@NonNull BiFunction<E, G, G> subTreeMapper) {
		return Collector.of(
				Hierarchy::new,
				(result, element) -> result.addWithCfg(
						valueMapper.apply(element),
						cfg -> optionsMapper.apply(element, new Cfg()),
						lvl -> subTreeMapper.apply(element, (G) (new Hierarchy<>()))
				),
				(result1, result2) -> result1.add(result2.subTrees)
		);
	}

	private Hierarchy<T, G> add(Set<SubTree<T, ?>> subTrees) {
		this.subTrees.addAll(subTrees);
		return this;
	}

}
