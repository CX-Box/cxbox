package org.cxbox.core.dto.rowmeta;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cxbox.model.core.entity.BaseEntity;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Setter
@Getter
@Table(name = "LOCK_BASE")
@NoArgsConstructor
public class LockStatus extends BaseEntity {

	@Enumerated(value = EnumType.STRING)
	private LockStatusType status;

	@Enumerated(value = EnumType.STRING)
	private CreateStatusType createStatus;

	@CreationTimestamp
	@Column(name = "lock_date", nullable = false)
	private LocalDateTime lockTime;

	public LockStatus(LockStatusType status, CreateStatusType createStatus) {
		this.status = status;
		this.createStatus = createStatus;
	}

}
