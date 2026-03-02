package majorproject.maf.dto.response;

import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreferenceResponse {

    private String investmentGoals;

    private String riskTolerance;

    private String preferredAsset;

    private Set<SectorDto> sectorIds;

    private Set<CompanyDto> companyIds;
}
