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

package org.cxbox.model.core.entity;

import org.cxbox.api.data.dictionary.LOV;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "DIVISION")
public class Division extends BaseEntity implements Serializable {

	private String name;

	private String fullName;

	private Boolean active;

	private LOV levelCd;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_ID")
	private Division parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEPT_ID")
	private Department department;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "HEAD_USER_ROLE_ID")
	private UserRole divisionHead;

	private Boolean checkGuFlg;

	private LOV headOfficeCd;

	public Division getParentByLevelCode(LOV divisionLevelCd) {
		Division result = this;
		while (result != null && !(Objects.equals(divisionLevelCd, result.getLevelCd()))) {
			result = result.getParent();
		}
		return result;
	}

}
