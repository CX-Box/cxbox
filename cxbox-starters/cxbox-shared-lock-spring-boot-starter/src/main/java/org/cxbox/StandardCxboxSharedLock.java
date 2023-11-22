package org.cxbox;

import java.time.LocalDateTime;
import org.cxbox.core.dto.rowmeta.LockStatus;
import org.cxbox.core.dto.rowmeta.LockStatusType;
import org.cxbox.core.metahotreload.CxboxSharedLock;
import org.cxbox.core.metahotreload.conf.properties.MetaConfigurationProperties;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.service.MetaLockService;

public class StandardCxboxSharedLock implements CxboxSharedLock {

	private final MetaConfigurationProperties config;

	private final MetaLockService metaLockService;

	private final JpaDao jpaDao;

	public StandardCxboxSharedLock(MetaConfigurationProperties config, MetaLockService metaLockService, JpaDao jpaDao) {
		this.config = config;
		this.metaLockService = metaLockService;
		this.jpaDao = jpaDao;
	}

	@Override
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
			;
			runnable.run();
		} finally {
			metaLockService.updateLock(LockStatusType.UNLOCK);
		}
	}

	@Override
	public void waitForLock() {
		LockStatus lockStatus = metaLockService.getLockEntity();

		while (lockStatus.getStatus().equals(LockStatusType.LOCK)) {
			if (lockStatus.getLockTime().plusSeconds(config.getBaseLockTimer()).isBefore(LocalDateTime.now())) {
				break;
			}
			try {
				Thread.sleep(config.getCheckLockInterval());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			lockStatus = jpaDao.findById(LockStatus.class, 1L);
		}
	}

}
