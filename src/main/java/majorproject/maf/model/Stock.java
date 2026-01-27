package majorproject.maf.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import majorproject.maf.model.user.User;

@Entity
@Table(name = "holdings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    private String symbol;

    @Override
    public String toString() {
        return "Stock{" +
                "shares=" + shares +
                ", symbol='" + symbol + '\'' +
                ", user=" + user +
                '}';
    }

    private long shares;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
