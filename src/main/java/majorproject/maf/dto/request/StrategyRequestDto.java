package majorproject.maf.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import majorproject.maf.model.enums.RebalancingFrequency;
import majorproject.maf.model.enums.RiskProfile;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StrategyRequestDto {
    private String strategyType;
    private String goal;
    private Integer timeHorizonMonths;
    private RiskProfile riskProfile;
    private Map<String, Integer> targetAllocation; // or Map<String, BigDecimal>
    private Map<String, Integer> sectorLimits;
    private RebalancingFrequency rebalancingFrequency;
}