package majorproject.maf.repository;

import majorproject.maf.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Integer> {

    UserPreferences findByUserId(int id);
}
