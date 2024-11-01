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
import java.util.function.UnaryOperator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.cxbox.api.data.dto.hierarhy.grouping.Config.Cfg;

@Getter
@NoArgsConstructor
public class Hierarchy<T, G extends Hierarchy<?, ?>> {

	private final Set<SubTree<T, ?>> subTrees = new HashSet<>();

	public Hierarchy<T, G> add(@NonNull T value) {
		subTrees.add(new SubTree<>(value, null, null));
		return this;
	}

	public Hierarchy<T, G> add(@NonNull T value, @NonNull UnaryOperator<G> childHierarchy) {
		var hb = (G) (new Hierarchy<>());
		subTrees.add(new SubTree<>(value, childHierarchy.apply(hb).getSubTrees(), null));
		return this;
	}

	public Hierarchy<T, G> addWithCfg(@NonNull T value, @NonNull UnaryOperator<Cfg> cfg) {
		subTrees.add(new SubTree<>(value, null, cfg.apply(new Cfg()).build().getOptions()));
		return this;
	}

	public Hierarchy<T, G> addWithCfg(@NonNull T value, @NonNull UnaryOperator<Cfg> cfg, @NonNull UnaryOperator<G> childHierarchy) {
		var hb = (G) (new Hierarchy<>());
		subTrees.add(new SubTree<>(value, childHierarchy.apply(hb).getSubTrees(), cfg.apply(new Cfg()).build().getOptions()));
		return this;
	}

}
