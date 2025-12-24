package majorproject.maf.dto;


import lombok.Data;

@Data
public class Share {
    private String symbol;
    private Long quantity;
    private Double price;
    private String id;

    public Share(String symbol, Long quantity) {
        this.symbol = symbol;
        this.quantity = quantity;
    }
}
