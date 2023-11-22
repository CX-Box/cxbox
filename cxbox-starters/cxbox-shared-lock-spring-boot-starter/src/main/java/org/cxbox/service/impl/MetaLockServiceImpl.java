package org.cxbox.service.impl;


import java.time.LocalDateTime;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import lombok.AllArgsConstructor;
import org.cxbox.core.dto.rowmeta.LockStatus;
import org.cxbox.core.dto.rowmeta.LockStatusType;
import org.cxbox.core.dto.rowmeta.LockStatus_;
import org.cxbox.model.core.dao.JpaDao;
import org.cxbox.service.MetaLockService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MetaLockServiceImpl implements MetaLockService {

	private final JpaDao jpaDao;

	@Override
	public LockStatus getLockEntity() {
		return jpaDao.findById(LockStatus.class, 1L);
	}

	@Override
	public boolean isLock() {
		return jpaDao.findById(LockStatus.class, 1L).getStatus().equals(LockStatusType.LOCK);
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public void createLockRowIfNotExist() {
		boolean exists = jpaDao.exists(LockStatus.class, (root, cq, cb) -> cb.equal(root.get(LockStatus_.id), 1L));
		if (!exists) {
			LockStatus lockStatus = new LockStatus(LockStatusType.UNLOCK);
			lockStatus.setId(1L);
			jpaDao.save(lockStatus);
		}

	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public int updateLock(LockStatusType status) {
		LockStatusType currentStatus = getLockEntity().getStatus();
		if (!currentStatus.equals(status)) {
			int updateResult = jpaDao.update(
					LockStatus.class, (root, cq, cb) -> cb.and(
							cb.equal(root.get(LockStatus_.id), 1L)
					),
					(update, root, cb) -> update.set(
							root.get(LockStatus_.status),
							cb.literal(status)
					)
			);
			if (updateResult == 1 && status.equals(LockStatusType.LOCK)) {
				updateLockTime(LocalDateTime.now());
			}
			return updateResult;
		}
		return 0;
	}

	@Override
	public void updateLockTime(LocalDateTime lockTime) {
		jpaDao.update(
				LockStatus.class, (root, cq, cb) -> cb.and(
						cb.equal(root.get(LockStatus_.id), 1L)
				),
				(update, root, cb) -> update.set(
						root.get(LockStatus_.lockTime),
						cb.literal(lockTime)
				)
		);
	}

}
