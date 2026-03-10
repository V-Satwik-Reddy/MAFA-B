package majorproject.maf.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalPricesWrapperDto {
    ArrayList<StockPriceDto> historicalPrices;
}
