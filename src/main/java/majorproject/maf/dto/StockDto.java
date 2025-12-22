package majorproject.maf.dto;

import lombok.Data;

@Data
public class StockDto {
    private String symbol;
    private long shares;
    private double totalAmount;
    private double currentPrice;
    private double avgBuyPrice;
    private double gainLoss;
}
