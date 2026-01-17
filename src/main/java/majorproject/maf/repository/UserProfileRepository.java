package majorproject.maf.repository;

import jakarta.transaction.Transactional;
import majorproject.maf.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    UserProfile findByUserId(int userId);

    UserProfile findByUsername(String username);

    @Modifying
    @Transactional
    @Query("""
        UPDATE UserProfile u
        SET u.balance = u.balance + :delta
        WHERE u.id  = :id
    """)
    void creditBalance(int id, double delta);

    @Modifying
    @Transactional
    @Query("""
        UPDATE UserProfile u
        SET u.balance = u.balance - :delta
        WHERE u.id  = :id
    """)
    void debitBalance(int id, double delta);
}
