package majorproject.maf.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceDto {
    private String symbol;
    private Double close;
    private LocalDate date;
    private Double open;
    private Double high;
    private Double low;
    private Long volume;
}
