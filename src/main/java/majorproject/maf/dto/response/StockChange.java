package majorproject.maf.dto.response;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
public class StockChange {
    private String symbol;
    private Double price;
    private Double change;
    private Double changePercent;
}
