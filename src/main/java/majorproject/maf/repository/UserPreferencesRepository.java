package majorproject.maf.repository;

import io.lettuce.core.dynamic.annotation.Param;
import majorproject.maf.model.user.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Integer> {

    UserPreferences findByUserId(int id);

    @Query("""
        SELECT p FROM UserPreferences p
        LEFT JOIN FETCH p.companies cp
        LEFT JOIN FETCH cp.company
        LEFT JOIN FETCH p.sectors sp
        LEFT JOIN FETCH sp.sector
        WHERE p.user.id = :userId
    """)
    UserPreferences findFullPreferences(@Param("userId") int userId);

}
