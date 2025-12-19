package majorproject.maf.controller;


import majorproject.maf.dto.BuyRequest;
import majorproject.maf.model.Coin;
import majorproject.maf.service.ExecutionService;
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
    public Coin binanceBuy(@RequestBody BuyRequest request) {
        return executionService.buyCoin(request, "binance");
    }
}
