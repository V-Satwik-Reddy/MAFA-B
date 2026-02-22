package majorproject.maf.controller;

import majorproject.maf.dto.request.StrategyRequestDto;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.dto.response.StrategyDto;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.service.StrategyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/strategy")
public class StrategyController {

    private final StrategyService strategyService;

    public StrategyController(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getUserStrategy(Authentication auth) {
        UserDto userDto = (UserDto) auth.getPrincipal();
        StrategyDto strategyDto = strategyService.getUserStrategy(userDto.getId());
        if(strategyDto != null) {
            return ResponseEntity.ok(ApiResponse.success("User strategy fetched successfully", strategyDto));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("No strategy found for user"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<?>> getUserStrategyHistory(Authentication auth) {
        UserDto userDto = (UserDto) auth.getPrincipal();
        List<StrategyDto> strategyHistory = strategyService.getUserStrategyHistory(userDto.getId());
        return ResponseEntity.ok(ApiResponse.success("User strategy history fetched successfully", strategyHistory)); // Replace null with actual history data
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> saveStrategy(Authentication auth, @RequestBody StrategyRequestDto req) {
        UserDto userDto = (UserDto) auth.getPrincipal();
        StrategyDto strategyDto = strategyService.addUserStrategy(userDto.getId(), req);
        return ResponseEntity.ok(ApiResponse.success("User strategy saved successfully", strategyDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateStrategy(Authentication auth, @PathVariable Long id, @RequestBody StrategyRequestDto req) {
        UserDto userDto = (UserDto) auth.getPrincipal();
        try {
            StrategyDto updated = strategyService.updateUserStrategy(userDto.getId(), id, req);
            return ResponseEntity.ok(ApiResponse.success("User strategy updated successfully", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ApiResponse.error("Strategy not found for user"));
        }
    }
}
