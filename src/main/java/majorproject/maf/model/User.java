package majorproject.maf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user",
        uniqueConstraints = {@UniqueConstraint(columnNames = "email")})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    private String username;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true) // ensures DB-level uniqueness
    private String email;

    @NotBlank
    private String password;

    private Long phone;

    private int balance;

    public User(@Email @NotBlank String email, @NotBlank String username, String encode, Long phone, int balance) {
    }
}
