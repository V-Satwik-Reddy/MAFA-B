package majorproject.maf.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDashboardDto {
    private String symbol;
    private long shares;
    private double totalAmount;
    private double currentPrice;
    private double avgBuyPrice;
    private double gainLoss;

}
