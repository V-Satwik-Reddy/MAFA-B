package majorproject.maf.repository;

import majorproject.maf.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    UserProfile findByUserId(int userId);

    UserProfile findByUsername(String username);
}
