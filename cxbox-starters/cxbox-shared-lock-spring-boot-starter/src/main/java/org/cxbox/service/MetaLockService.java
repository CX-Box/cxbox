package org.cxbox.service;

import java.time.LocalDateTime;
import org.cxbox.core.dto.rowmeta.LockStatus;
import org.cxbox.core.dto.rowmeta.LockStatusType;

public interface MetaLockService {

	LockStatus getLockEntity();

	boolean isLock();

	void createLockRowIfNotExist();

	int updateLock(LockStatusType status);

	void updateLockTime(LocalDateTime lockTime);

}
