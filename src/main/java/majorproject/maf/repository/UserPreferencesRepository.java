package majorproject.maf.repository;

import majorproject.maf.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Integer> {

    UserPreferences findByUserId(int id);

    @Query("""
        SELECT DISTINCT p FROM UserPreferences p
        LEFT JOIN FETCH p.sectors
        LEFT JOIN FETCH p.companies
        WHERE p.user.id = :id
        """)
    UserPreferences findFullPreferences(int id);

}
