package majorproject.maf.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
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
