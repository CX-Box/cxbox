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
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.NotAudited;

/**
 * User
 */
/*@Audited* is not audited by default in 4.0.0 cxbox major. Please audit on project level*/
@Entity
@Table(name = "users") // users, а не user, т.к. это служебное слово oracle
@Getter
@Setter
public class User extends BaseEntity {

	private String login;

	private String firstName;

	private String lastName;

	private String patronymic;

	private String phone;

	private String email;

	private String fullUserName;

	private String title;

	@Column(name = "ext_attr_11")
	private String extensionAttribute11;

	@Column(name = "ext_attr_12")
	private String extensionAttribute12;

	@Column(name = "ext_attr_13")
	private String extensionAttribute13;

	@Column(name = "ext_attr_14")
	private String extensionAttribute14;

	@Column(name = "ext_attr_15")
	private String extensionAttribute15;

	@Column(name = "DN")
	private String dn;

	private String origDeptCode;

	@ManyToOne
	@Fetch(FetchMode.JOIN)
	@JoinColumn(name = "dept_id")
	private Department department;

	@JsonIgnore
	private String password;

	@Deprecated
	@Column(name = "internal_role_cd")
	private LOV internalRole;

	@NotAudited
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private List<UserRole> userRoleList;

	private LOV timezone;

	private LOV locale;

	private String userPrincipalName;

	@Column(name = "active")
	//@Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
	private Boolean active;

	public String getFullName() {
		StringBuilder sB = new StringBuilder();
		if (lastName != null) {
			sB.append(lastName);
			if (firstName != null || patronymic != null) {
				sB.append(" ");
			}
		}
		if (firstName != null) {
			sB.append(firstName);
			if (patronymic != null) {
				sB.append(" ");
			}
		}
		if (patronymic != null) {
			sB.append(patronymic);
		}
		return sB.toString();
	}

	public String getUserNameInitials() {
		StringBuilder sB = new StringBuilder();
		if (lastName != null) {
			sB.append(lastName);
			if (firstName != null || patronymic != null) {
				sB.append(" ");
			}
		}
		if (firstName != null) {
			sB.append(StringUtils.left(firstName, 1).toUpperCase() + ".");
			if (patronymic != null) {
				sB.append(" ");
			}
		}
		if (patronymic != null) {
			sB.append(StringUtils.left(patronymic, 1).toUpperCase() + ".");
		}
		return sB.toString();
	}

	public String getUserNameInitialsWithoutSpace() {
		StringBuilder sB = new StringBuilder();
		if (lastName != null) {
			sB.append(lastName);
			if (firstName != null || patronymic != null) {
				sB.append(" ");
			}
		}
		if (firstName != null) {
			sB.append(StringUtils.left(firstName, 1).toUpperCase() + ".");
		}
		if (patronymic != null) {
			sB.append(StringUtils.left(patronymic, 1).toUpperCase() + ".");
		}
		return sB.toString();
	}

	public ZoneId getZoneId() {
		return Optional.ofNullable(getTimezone())
				.map(t -> ZoneId.of(t.getKey()))
				.orElseGet(ZoneId::systemDefault);
	}

}
