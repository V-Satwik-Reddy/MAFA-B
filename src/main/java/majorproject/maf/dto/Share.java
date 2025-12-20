package majorproject.maf.model;


import lombok.Data;
import java.util.List;

@Data
public class Share {
    private String symbol;
    private double quantity;
    private double price;
    private String id;
}
