package majorproject.maf.repository;

import majorproject.maf.model.user.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    UserProfile findByUserId(int userId);

    UserProfile findByUsername(String username);

    @Modifying
    @Query("""
        UPDATE UserProfile u
        SET u.balance = u.balance + :delta
        WHERE u.id  = :id
    """)
    void creditBalance(int id, double delta);

    @Modifying
    @Query("""
    UPDATE UserProfile u
    SET u.balance = u.balance - :amount
    WHERE u.id = :id AND u.balance >= :amount
""")
    int debitIfSufficientBalance(int id, double totalCost);
}
