package majorproject.maf.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PreferenceResponse {

    private String investmentGoals;

    private String riskTolerance;

    private String preferredAsset;

    private Set<SectorDto> sectorIds;

    private Set<CompanyDto> companyIds;
}
