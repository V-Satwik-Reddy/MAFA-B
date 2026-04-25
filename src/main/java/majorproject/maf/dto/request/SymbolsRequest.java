package majorproject.maf.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymbolsRequest {
    private List<String> symbols;

    @JsonProperty("symbols")
    public void setSymbols(List<String> symbols) {
        if (symbols != null) {
            this.symbols = symbols.stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .toList();
        }
    }
}
