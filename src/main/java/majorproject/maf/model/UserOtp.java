package majorproject.maf.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;


@Entity
@Table(name = "user_otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserOtp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.SEQUENCE)
    private Long id;

    private String email;

    private String otp;

    private Long expiresAt;

    public UserOtp(String email, String otp, long l) {
        this.email = email;
        this.otp = otp;
        this.expiresAt = l;
    }
}
