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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cxbox.api.util.compare.comparator;

import java.util.Comparator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.util.compare.common.Transformer;

@RequiredArgsConstructor
@EqualsAndHashCode
public class TransformingComp<I, O> implements Comparator<I> {

	private final Comparator<O> decorated;

	private final Transformer<? super I, ? extends O> transformer;

	@Override
	public int compare(final I obj1, final I obj2) {
		final O value1 = this.transformer.transform(obj1);
		final O value2 = this.transformer.transform(obj2);
		return this.decorated.compare(value1, value2);
	}

}

