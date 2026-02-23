package majorproject.maf.repository;

import majorproject.maf.model.Alert;
import majorproject.maf.model.enums.AlertCondition;
import majorproject.maf.model.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUserId(Integer userId);

    List<Alert> findByUserIdAndStatus(Integer userId, AlertStatus status);

    Alert findByUserIdAndId(Integer userId, Long alertId);
}
