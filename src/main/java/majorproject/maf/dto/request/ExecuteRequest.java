package majorproject.maf.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteRequest {
    private long quantity;
    private String symbol;
    public void setSymbol(String symbol) {
        if (symbol != null) {
            this.symbol = symbol.trim().toUpperCase();
        } else {
            this.symbol = null;
        }
    }
}