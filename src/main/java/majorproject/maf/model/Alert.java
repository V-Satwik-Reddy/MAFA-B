package majorproject.maf.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import majorproject.maf.model.enums.AlertCondition;
import majorproject.maf.model.enums.AlertStatus;
import majorproject.maf.model.enums.Channel;
import majorproject.maf.model.user.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String symbol;

    @Enumerated(EnumType.STRING)
    private AlertCondition alertCondition;

    private Double targetPrice;

    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    private LocalDateTime createdAt;

    private LocalDateTime triggeredAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
