package majorproject.maf.repository;


import majorproject.maf.model.Chat;
import majorproject.maf.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findAllByUserIdOrderByCreatedAtDesc(Integer user_Id);
}
