package majorproject.maf.dto.request;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SymbolsRequest {
    List<String> symbols;
}
