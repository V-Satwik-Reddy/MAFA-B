package majorproject.maf.controller;

import majorproject.maf.dto.response.StockDto;
import majorproject.maf.dto.response.TransactionDto;
import majorproject.maf.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DashboardController {

    DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(Authentication auth) {
        String email = auth.getName();
        List<TransactionDto> txns = dashboardService.getUserTransactions(email);
        return ResponseEntity.status(HttpStatus.OK).body(txns);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(Authentication auth) {
        String email = auth.getName();
        List<StockDto> holdings =  dashboardService.getHoldingsDetails(email);
        return ResponseEntity.status(HttpStatus.OK).body(holdings);
    }
}
