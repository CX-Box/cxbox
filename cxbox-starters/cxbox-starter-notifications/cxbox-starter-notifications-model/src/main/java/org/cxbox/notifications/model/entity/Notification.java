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

package org.cxbox.notifications.model.entity;


import org.cxbox.api.data.dictionary.LOV;
import org.cxbox.model.core.entity.BaseEntity;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "NOTIFICATION")
public class Notification extends BaseEntity {

	@Column(name = "SUBJECT")
	private String subject;

	@Column(name = "UI_SUBJECT")
	private String uiSubject;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "MESSAGE")
	private String message;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "UI_MESSAGE")
	private String uiMessage;

	@Formula("BITAND(DELIVERY_STATUS, 1)")
	private Integer readBit;

	@Formula("BITAND(DELIVERY_TYPE, 1)")
	private Integer pushBit;

	@Column(name = "URL")
	private String url;

	@Column(name = "USER_ID", nullable = false)
	private Long recipientId;

	@Column(name = "DELIVERY_TYPE")
	private int deliveryType;

	@Column(name = "DELIVERY_STATUS")
	private int deliveryStatus;

	@Column(name = "MIME_TYPE_CD")
	private LOV mimeType;

	@Column(name = "EVENT_NAME_CD")
	private LOV eventName;

	@PreUpdate
	@PrePersist
	public final void prePersist() {
		this.subject = StringUtils.truncate(this.subject, 1500);
		this.uiSubject = StringUtils.truncate(this.uiSubject, 1500);
	}

	@Transient
	public boolean isRead() {
		return readBit == 1;
	}

	@Transient
	public boolean isPush() {
		return pushBit == 1;
	}

}
