package majorproject.maf.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistRequest {
    String symbol;
    public void setSymbol(String symbol) {
        if (symbol != null) {
            this.symbol = symbol.trim().toUpperCase();
        } else {
            this.symbol = null;
        }
    }
}
