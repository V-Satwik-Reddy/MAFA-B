package majorproject.maf.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import majorproject.maf.model.enums.RebalancingFrequency;
import majorproject.maf.model.enums.RiskProfile;
import majorproject.maf.model.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "investment_strategy")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String strategyType;        // "moderate_growth"
    private String goal;                // free text

    private Integer timeHorizonMonths;  // 60

    @Enumerated(EnumType.STRING)
    private RiskProfile riskProfile;    // CONSERVATIVE, MODERATE, AGGRESSIVE...

    // store flexible structures as JSON text for now
    @Column(columnDefinition = "TEXT")
    private String targetAllocationJson;  // {"equity":70,"debt":20,"cash":10}

    @Column(columnDefinition = "TEXT")
    private String sectorLimitsJson;      // {"Technology":35,"Financials":20}

    @Enumerated(EnumType.STRING)
    private RebalancingFrequency rebalancingFrequency; // MONTHLY, QUARTERLY...

    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
