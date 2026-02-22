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
}
