package org.cxbox.service;

import org.cxbox.model.LockStatus;
import org.cxbox.model.LockStatusType;

public interface MetaLockService {


	LockStatus getLockEntity();

	boolean isLock();

	void createLockRowIfNotExist();

	int updateLock(LockStatusType status);

}
