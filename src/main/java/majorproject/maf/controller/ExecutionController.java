package majorproject.maf.controller;


import majorproject.maf.dto.BuyRequest;
import majorproject.maf.dto.SellRequest;
import majorproject.maf.dto.Share;
import majorproject.maf.model.Transaction;
import majorproject.maf.repository.StockRepository;
import majorproject.maf.repository.TransactionRepository;
import majorproject.maf.service.ExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("execute")
public class ExecutionController {

    private final ExecutionService executionService;
    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/buy")
    public ResponseEntity<?> binanceBuy(@RequestBody BuyRequest request, Authentication authentication) {
        Share s=executionService.buyShares(request,authentication.getName());
        return ResponseEntity.ok(s);
    }

    @PostMapping("/sell")
    public ResponseEntity<?> binanceSell(@RequestBody SellRequest request, Authentication authentication) {
        Share s=executionService.sellShares(request,authentication.getName());
        return ResponseEntity.ok(s);
    }
}
