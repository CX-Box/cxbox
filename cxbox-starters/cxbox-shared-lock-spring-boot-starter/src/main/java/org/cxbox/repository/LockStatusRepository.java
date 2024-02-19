package org.cxbox.repository;

import java.time.LocalDateTime;
import org.cxbox.model.LockStatus;
import org.cxbox.model.LockStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LockStatusRepository extends JpaRepository<LockStatus, Long> {

	@Modifying
	@Query("UPDATE LockStatus ls SET ls.status = :status, ls.lockTime = :lockTime WHERE ls.id = :id")
	int updateStatusAndLockTimeById(@Param("status") LockStatusType statusType,
			@Param("lockTime") LocalDateTime lockTime,
			@Param("id") long id);

}
