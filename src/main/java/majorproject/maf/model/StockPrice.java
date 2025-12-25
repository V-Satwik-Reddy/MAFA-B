package majorproject.maf.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "stock_prices")
@Getter
@Setter
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private LocalDate date;

    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
}
