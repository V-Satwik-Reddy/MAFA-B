package majorproject.maf.dto;

import java.time.LocalDateTime;

public record TransactionDto(
        Long id,
        String type,
        String asset,
        Double assetQuantity,
        Double amount,
        LocalDateTime createdAt
) {
    @Override
    public String toString() {
        return "TransactionDto{" +
                "amount=" + amount +
                ", id=" + id +
                ", type='" + type + '\'' +
                ", asset='" + asset + '\'' +
                ", assetQuantity=" + assetQuantity +
                ", createdAt=" + createdAt +
                '}';
    }
}

