package majorproject.maf.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TransactionDto{

    private Long id;
    private String type;
    private String asset;
    private Long assetQuantity;
    private Double amount;
    private LocalDateTime createdAt;
}

