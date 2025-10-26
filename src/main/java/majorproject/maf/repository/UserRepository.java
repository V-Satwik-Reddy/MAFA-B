package majorproject.maf.repository;

import lombok.NonNull;
import majorproject.maf.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(@NonNull String email);
}
