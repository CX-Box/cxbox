package org.cxbox.service.impl;


import java.time.LocalDateTime;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.cxbox.model.LockStatus;
import org.cxbox.model.LockStatusType;
import org.cxbox.repository.LockStatusRepository;
import org.cxbox.service.MetaLockService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetaLockServiceImpl implements MetaLockService {

	private final LockStatusRepository repository;

	private final EntityManager entityManager;

	public final long ROW_LOCK_INDEX = 1L;

	@Override
	public LockStatus getLockEntity() {
		return entityManager.find(LockStatus.class, ROW_LOCK_INDEX);
	}

	@Override
	public boolean isLock() {
		LockStatus currentLockStatus = getLockEntity();
		return currentLockStatus.getStatus().equals(LockStatusType.LOCK);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createLockRowIfNotExist() {
		boolean exists = repository.existsById(ROW_LOCK_INDEX);
		if (!exists) {
			LockStatus lockStatus = new LockStatus(LockStatusType.UNLOCK);
			lockStatus.setId(ROW_LOCK_INDEX);
			try {
				repository.save(lockStatus);
			} catch (DataIntegrityViolationException ignore) {

			}
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int updateLock(LockStatusType status) {
		LockStatusType currentStatus = getLockEntity().getStatus();
		if (!currentStatus.equals(status)) {
			return repository.updateStatusAndLockTimeById(status, LocalDateTime.now(), ROW_LOCK_INDEX);
		}
		return 0;
	}

}
