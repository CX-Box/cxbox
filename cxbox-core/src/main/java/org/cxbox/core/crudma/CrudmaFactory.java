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

package org.cxbox.core.crudma;

import java.util.Optional;
import org.cxbox.core.crudma.bc.impl.BcDescription;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CrudmaFactory {

	@Autowired
	private List<Crudma> crudmaList;

	public Crudma get(BcDescription bcDescription) {
		return crudmaList.stream()
				.filter(crudma -> applyFilter(crudma, bcDescription))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"Can't find bean with class " + bcDescription.getCrudmaService().getSimpleName()));
	}

	@SneakyThrows
	private boolean applyFilter(Crudma crudma, BcDescription bcDescription) {
		if (AopUtils.isAopProxy(crudma)) {
			Advised advised = (Advised) crudma;
			Class<?> aClass = Optional.of(advised)
					.map(Advised::getTargetSource)
					.map(src -> {
						try {
							return src.getTarget();
						} catch (Exception ex) {
							throw new IllegalStateException(ex);
						}
					})
					.map(Object::getClass)
					.orElse(null);
			return ClassUtils.isAssignable(aClass, bcDescription.getCrudmaService());
		} else {
			return Objects.equals(crudma.getClass(), bcDescription.getCrudmaService());
		}
	}

}
