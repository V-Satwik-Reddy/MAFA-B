package majorproject.maf.controller;

import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.dto.response.StockDashboardDto;
import majorproject.maf.dto.response.TransactionDto;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.model.enums.Period;
import majorproject.maf.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class DashboardController {

    DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactions(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Period period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime afterDate,
            Authentication auth) {
        UserDto u=(UserDto) auth.getPrincipal();
        List<TransactionDto> txns = dashboardService.getUserTransactions(u.getId(), limit, page, period, startDate, endDate, beforeDate, afterDate);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Users Transactions fetched successfully", txns));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<List<StockDashboardDto>>> getDashboardData(Authentication auth) {
        UserDto u=(UserDto) auth.getPrincipal();
        List<StockDashboardDto> holdings =  dashboardService.getHoldingsDetails(u.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Users Holdings fetched successfully", holdings));
    }
}
