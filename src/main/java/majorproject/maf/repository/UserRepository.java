package majorproject.maf.repository;

import lombok.NonNull;
import majorproject.maf.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(@NonNull String email);

    @Modifying
    @Query("""
        UPDATE User u
        SET u.balance = u.balance - :delta
        WHERE u.email = :email 
    """)
    int debitBalance(String email, double delta);

    @Modifying
    @Query("""
        UPDATE User u
        SET u.balance = u.balance + :delta
        WHERE u.email = :email 
    """)
    int creditBalance(String email, double delta);
}
