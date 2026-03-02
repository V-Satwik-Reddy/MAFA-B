package majorproject.maf.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistDto {

    private CompanyDto company;

    private LocalDateTime addedAt;

}
