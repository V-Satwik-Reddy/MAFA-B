package majorproject.maf.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import majorproject.maf.model.enums.RebalancingFrequency;
import majorproject.maf.model.enums.RiskProfile;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StrategyDto {
    private Long id;
    private String strategyType;
    private String goal;
    private Integer timeHorizonMonths;
    private RiskProfile riskProfile;
    private Map<String, Integer> targetAllocation;
    private Map<String, Integer> sectorLimits;
    private RebalancingFrequency rebalancingFrequency;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
