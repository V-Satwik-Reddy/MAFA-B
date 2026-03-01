package majorproject.maf.controller;

import majorproject.maf.dto.request.ExecuteRequest;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.dto.response.TransactionDto;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.service.ExecutionService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<TransactionDto>> binanceBuy(@RequestBody ExecuteRequest request, Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        TransactionDto t =executionService.buyShares(request,u.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Stock bought successfully", t));
    }

    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<TransactionDto>> binanceSell(@RequestBody ExecuteRequest request, Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        TransactionDto t=executionService.sellShares(request,u.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Stock sold successfully", t));
    }
}
