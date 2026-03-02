package majorproject.maf.dto.response;

import lombok.*;
import majorproject.maf.model.enums.RebalancingFrequency;
import majorproject.maf.model.enums.RiskProfile;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyDto {
    private Long id;
    private String strategyType;
    private String goal;
    private Integer timeHorizonMonths;
    private RiskProfile riskProfile;
    private Map<String, Double> targetAllocation;
    private Map<String, Double> sectorLimits;
    private RebalancingFrequency rebalancingFrequency;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
