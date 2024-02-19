package org.cxbox.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.cxbox.config.SharedLockConfigurationProperties;
import org.cxbox.core.metahotreload.CxboxSharedLock;
import org.cxbox.model.LockStatus;
import org.cxbox.model.LockStatusType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class StandardCxboxSharedLock implements CxboxSharedLock {

	private final SharedLockConfigurationProperties config;

	private final MetaLockService metaLockService;

	public StandardCxboxSharedLock(SharedLockConfigurationProperties config,
			MetaLockService metaLockService) {
		this.config = config;
		this.metaLockService = metaLockService;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void acquireAndExecute(Runnable runnable) {
		try {
			metaLockService.createLockRowIfNotExist();
			if (metaLockService.isLock()) {
				waitForLock();
			}

			int updateResult = metaLockService.updateLock(LockStatusType.LOCK);
			while (updateResult != 1) {
				waitForLock();
				updateResult = metaLockService.updateLock(LockStatusType.LOCK);
			}
			runnable.run();
		} finally {
			metaLockService.updateLock(LockStatusType.UNLOCK);
		}
	}

	@Override
	public void waitForLock() {
		LockStatus lockStatus = metaLockService.getLockEntity();

		while (lockStatus.getStatus().equals(LockStatusType.LOCK)) {

			if (LocalDateTime.now().isAfter(lockStatus.getLockTime()
					.plus(config.getTimeout(), ChronoUnit.MILLIS))) {
				metaLockService.updateLock(LockStatusType.UNLOCK);
				break;
			}
			try {
				Thread.sleep(config.getCheckInterval());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			lockStatus = metaLockService.getLockEntity();
		}
	}

}
