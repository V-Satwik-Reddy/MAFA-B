package majorproject.maf.dto.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
