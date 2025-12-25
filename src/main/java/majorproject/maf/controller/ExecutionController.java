package majorproject.maf.controller;

import majorproject.maf.dto.request.ExecuteRequest;
import majorproject.maf.dto.response.TransactionDto;
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
    public ResponseEntity<?> binanceBuy(@RequestBody ExecuteRequest request, Authentication authentication) throws Exception {
        TransactionDto t =executionService.buyShares(request,authentication.getName());
        return ResponseEntity.ok(t);
    }

    @PostMapping("/sell")
    public ResponseEntity<?> binanceSell(@RequestBody ExecuteRequest request, Authentication authentication) {
        TransactionDto t=executionService.sellShares(request,authentication.getName());
        return ResponseEntity.ok(t);
    }
}
