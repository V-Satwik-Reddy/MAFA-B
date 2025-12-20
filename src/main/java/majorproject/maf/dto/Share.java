package majorproject.maf.dto;


import lombok.Data;

@Data
public class Share {
    private String symbol;
    private double quantity;
    private double price;
    private String id;
}
