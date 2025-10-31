package majorproject.maf.model;


import lombok.Data;
import java.util.List;

@Data
public class Coin {
    private String coinName;
    private double quantity;
    private double price;
    private List<String> coinId;
    public Coin(String coinName, double quantity, double price, List<String> coinId) {
        this.coinName = coinName;
        this.quantity = quantity;
        this.price = price;
        this.coinId=coinId;
    }
}
