package majorproject.maf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @Email @NotBlank @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    private String password;

    @Column
    private LocalDateTime createdAt;

    @Column
    private boolean isEmailVerified;

    @Column
    private boolean isPhoneVerified=false;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stock> stocks = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chat> chats = new ArrayList<>();

    public User(@Email @NotBlank String email, String password) {
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.isEmailVerified = false;
        this.status= UserStatus.ACTIVE;
    }
}
enum UserStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED,
    PENDING
}