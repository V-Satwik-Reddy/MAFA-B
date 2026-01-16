package majorproject.maf.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PreferenceRequest {

    private String investmentGoals;

    private String riskTolerance;

    private String preferredAsset;

    private List<String> sectors;

    private List<String> companies;
}
