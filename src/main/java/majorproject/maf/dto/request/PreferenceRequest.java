package majorproject.maf.dto.request;

import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreferenceRequest {

    private String investmentGoals;

    private String riskTolerance;

    private String preferredAsset;

    private Set<Long> sectorIds;

    private Set<Long> companyIds;
}
