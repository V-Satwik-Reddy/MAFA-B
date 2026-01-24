package majorproject.maf.dto.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@Getter
@Setter
@AllArgsConstructor
public class StockChange implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String symbol;
    private Double price;
    private Double change;
    private Double changePercent;
}
