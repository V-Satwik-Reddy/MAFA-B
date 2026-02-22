package majorproject.maf.repository;


import majorproject.maf.model.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findAllByUserIdOrderByCreatedAtDesc(int id);

    List<Chat> findByUserIdOrderByCreatedAtDesc(int id, Pageable pageable);
}
