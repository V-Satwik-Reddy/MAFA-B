package majorproject.maf.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import majorproject.maf.model.serving.CompanyMaster;

import java.time.LocalDateTime;

@Entity
@Table(name="watchlist")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private CompanyMaster company;

    @Column(name = "created_at")
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
    }
}
