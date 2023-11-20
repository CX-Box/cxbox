package org.cxbox.core.metahotreload;

import java.time.LocalDateTime;
import org.cxbox.core.dto.rowmeta.LockStatus;
import org.cxbox.core.dto.rowmeta.LockStatusType;

public interface MetaLockService {

	void updateLock(LockStatusType status);

	boolean isLock();

	boolean isCreate();

	void doCreate();

	void createLockRowIfNotExist();

	void updateLockTime(LocalDateTime lockTime);

	LockStatus getLockEntity();

}
