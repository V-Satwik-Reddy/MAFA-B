package majorproject.maf.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import majorproject.maf.dto.request.StrategyRequestDto;
import majorproject.maf.dto.response.StrategyDto;
import majorproject.maf.model.InvestmentStrategy;
import majorproject.maf.repository.StrategyRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class StrategyService {

    private final StrategyRepository strategyRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public StrategyService(StrategyRepository strategyRepository, ObjectMapper objectMapper, UserRepository userRepository) {
        this.strategyRepository = strategyRepository;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    public StrategyDto getUserStrategy(int userId) {
        InvestmentStrategy strategy = strategyRepository.findFirstByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
        if (strategy == null) {
            return null; // Or throw an exception if preferred
        }
        return toDto(strategy);
    }

    public List<StrategyDto> getUserStrategyHistory(int id) {
        List<InvestmentStrategy> strategies= strategyRepository.findAllByUserIdOrderByCreatedAtDesc(id);
        return strategies.stream().map(this::toDto).toList();
    }

    public StrategyDto addUserStrategy(int id,StrategyRequestDto req) {
        InvestmentStrategy strategy = new InvestmentStrategy();
        strategy.setStrategyType(req.getStrategyType());
        strategy.setGoal(req.getGoal());
        strategy.setTimeHorizonMonths(req.getTimeHorizonMonths());
        strategy.setRiskProfile(req.getRiskProfile());
        strategy.setTargetAllocationJson(writeMap(req.getTargetAllocation()));
        strategy.setSectorLimitsJson(writeMap(req.getSectorLimits()));
        strategy.setRebalancingFrequency(req.getRebalancingFrequency());
        strategy.setIsActive(true);
        // Set userId from authentication context in real implementation
        strategy.setUser(userRepository.getReferenceById(id)); // Placeholder
        strategyRepository.markExistingStrategiesInactive(id); // Custom method to mark existing strategies as inactive
        strategyRepository.save(strategy);
        return toDto(strategy);
    }

    public StrategyDto updateUserStrategy(int userId, Long strategyId, StrategyRequestDto req) {
        InvestmentStrategy strategy = strategyRepository.findByIdAndUserId(strategyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found for user"));

        if (req.getStrategyType() != null) {
            strategy.setStrategyType(req.getStrategyType());
        }
        if (req.getGoal() != null) {
            strategy.setGoal(req.getGoal());
        }
        if (req.getTimeHorizonMonths() != null) {
            strategy.setTimeHorizonMonths(req.getTimeHorizonMonths());
        }
        if (req.getRiskProfile() != null) {
            strategy.setRiskProfile(req.getRiskProfile());
        }
        if (req.getTargetAllocation() != null) {
            strategy.setTargetAllocationJson(writeMap(req.getTargetAllocation()));
        }
        if (req.getSectorLimits() != null) {
            strategy.setSectorLimitsJson(writeMap(req.getSectorLimits()));
        }
        if (req.getRebalancingFrequency() != null) {
            strategy.setRebalancingFrequency(req.getRebalancingFrequency());
        }
        // You probably don't let client change isActive from here

        InvestmentStrategy saved = strategyRepository.save(strategy);
        return toDto(saved);
    }

    public StrategyDto toDto(InvestmentStrategy strategy) {
        Map<String, Integer> targetAllocation = readMap(strategy.getTargetAllocationJson());
        Map<String,Integer> sectorLimits = readMap(strategy.getSectorLimitsJson());
        return new StrategyDto(
                strategy.getId(),
                strategy.getStrategyType(),
                strategy.getGoal(),
                strategy.getTimeHorizonMonths(),
                strategy.getRiskProfile(),
                targetAllocation,
                sectorLimits,
                strategy.getRebalancingFrequency(),
                strategy.getIsActive(),
                strategy.getCreatedAt(),
                strategy.getUpdatedAt()
        );
    }

    private Map<String, Integer> readMap(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Invalid JSON in strategy", e);
        }
    }

    private String writeMap(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) return "{}";
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize strategy map", e);
        }
    }
}
