package majorproject.maf.repository;

import majorproject.maf.model.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOtpRepository extends JpaRepository<UserOtp, Long> {

    UserOtp findByEmail(String email);

}
