package majorproject.maf.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import majorproject.maf.model.user.User;

import java.time.LocalDate;

@Entity
@Table(name = "portfolio_daily_snapshot")
@Getter
@Setter
@NoArgsConstructor
public class PortfolioDailySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate date;

    private Double totalValue;
    private Double cashBalance;
    private Double investedValue;

//    private String currency; // e.g. "USD", optional

    // createdAt/updatedAt if you like
}
