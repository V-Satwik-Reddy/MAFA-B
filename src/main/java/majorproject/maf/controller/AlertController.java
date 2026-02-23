package majorproject.maf.controller;

import majorproject.maf.dto.request.AlertRequestDto;
import majorproject.maf.dto.response.AlertResponseDto;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.model.enums.AlertStatus;
import majorproject.maf.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAlert(@RequestBody AlertRequestDto alert, Authentication authentication) {
        UserDto userDto = (UserDto) authentication.getPrincipal();
        AlertResponseDto alertResponseDto =  alertService.createAlert(userDto,alert);
        return ResponseEntity.ok(ApiResponse.success("Alert Created", alertResponseDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAlerts(@RequestParam(required = false) AlertStatus status, Authentication authentication) {
        UserDto userDto = (UserDto) authentication.getPrincipal();
        List<AlertResponseDto> alerts=alertService.getUserAlerts(userDto.getId(),status);
        return ResponseEntity.ok(ApiResponse.success("Fetched User Alerts", alerts));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteAlert(@PathVariable long id, Authentication authentication) {
        UserDto userDto = (UserDto) authentication.getPrincipal();
        AlertResponseDto  alertResponseDto = alertService.deleteUserAlert(userDto.getId(),id);
        return ResponseEntity.ok(ApiResponse.success("Alert Deleted", alertResponseDto));
    }
}
