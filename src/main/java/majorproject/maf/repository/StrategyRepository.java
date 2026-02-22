package majorproject.maf.repository;

import jakarta.transaction.Transactional;
import majorproject.maf.model.InvestmentStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyRepository extends JpaRepository<InvestmentStrategy, Long> {

    InvestmentStrategy findFirstByUserIdAndIsActiveTrueOrderByCreatedAtDesc(int userId);

    List<InvestmentStrategy> findAllByUserIdOrderByCreatedAtDesc(int id);

    @Modifying
    @Query("update InvestmentStrategy s set s.isActive = false where s.user.id = :userId")
    @Transactional
    void markExistingStrategiesInactive(@Param("userId") int userId);

    Optional<InvestmentStrategy> findByIdAndUserId(Long strategyId, int userId);
}
