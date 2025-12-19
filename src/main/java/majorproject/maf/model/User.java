package majorproject.maf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user", uniqueConstraints = {@UniqueConstraint(columnNames = "email")})
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

    private double balance;

    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Transaction> transactions = new ArrayList<>();

    public User(@Email @NotBlank String email, @NotBlank String username, String encode, Long phone, int balance) {
        this.email = email;
        this.username = username;
        this.password = encode;
        this.phone = phone;
        this.balance = balance;
    }
}
