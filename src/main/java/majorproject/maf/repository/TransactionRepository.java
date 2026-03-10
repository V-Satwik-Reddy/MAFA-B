package majorproject.maf.repository;

import majorproject.maf.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Integer userId);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(Integer userId,Pageable pageable);

    List<Transaction> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Integer userId, LocalDateTime cutoff);

    List<Transaction> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Integer userId, LocalDateTime cutoff, Pageable pageable);

    // Date range: between startDate and endDate
    List<Transaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Integer userId, LocalDateTime start, LocalDateTime end);
    List<Transaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Integer userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Before a specific date
    List<Transaction> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(Integer userId, LocalDateTime before);
    List<Transaction> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(Integer userId, LocalDateTime before, Pageable pageable);
}
