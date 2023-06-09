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

package org.cxbox.core.crudma.bc;

import org.cxbox.core.crudma.bc.impl.BcDescription;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;


public interface BcRegistry {

	Collection<String> getAllBcNames();

	BcDescription getBcDescription(final String bcName);

	String getUrlFromBc(final String bcName);

	List<BcDescription> getBcHierarchy(final String bcName);

	void refresh();

	<T> Stream<T> select(Predicate<BcDescription> predicate, Function<BcDescription, T> transformer);

	default Stream<BcDescription> select(Predicate<BcDescription> predicate) {
		return select(predicate, Function.identity());
	}

	default <T extends BcDescription> Stream<T> select(Class<T> cls) {
		return select(cls::isInstance, cls::cast);
	}

}
