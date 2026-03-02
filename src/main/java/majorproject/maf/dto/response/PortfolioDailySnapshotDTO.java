package majorproject.maf.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDailySnapshotDTO {

    private LocalDate date;

    private Double totalValue;
    private Double cashBalance;
    private Double investedValue;

}
