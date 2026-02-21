package majorproject.maf.controller;

import majorproject.maf.dto.request.AddBalance;
import majorproject.maf.dto.request.WatchlistRequest;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.dto.response.Share;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.dto.response.WatchlistDto;
import majorproject.maf.service.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class PortfolioController {

    public PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/holdings")
    public ResponseEntity<ApiResponse<?>> getHoldings(Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        List<Share> holdings = portfolioService.getUserHoldings(u.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User Holdings fetched", holdings));
    }

    @PostMapping("/add-balance")
    public ResponseEntity<ApiResponse<?>> addBalance(@RequestBody AddBalance addBalance, Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        portfolioService.addBalance(u.getId(), addBalance.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successMessage("Balance added successfully"));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<?>> getBalance(Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        Double balance = portfolioService.getBalance(u.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User balance fetched", balance));
    }

    @GetMapping("/watchlist")
    public ResponseEntity<ApiResponse<?>> getWatchlist(Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        List<WatchlistDto> watchlist = portfolioService.getWatchlist(u.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User watchlist fetched", watchlist));
    }

    @PostMapping("/watchlist")
    public ResponseEntity<ApiResponse<?>> addToWatchlist(@RequestBody WatchlistRequest req, Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        int i=portfolioService.addToWatchlist(u.getId(), req.getSymbol());
        if(i==1) return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Company added to watchlist", Map.of("symbol", req.getSymbol(), "addedAt", LocalDateTime.now())));
        else
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Company already in watchlist"));
    }

    @DeleteMapping("/watchlist/{symbol}")
    public ResponseEntity<ApiResponse<?>> removeFromWatchlist(@PathVariable String symbol, Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        boolean done=portfolioService.removeFromWatchlist(u.getId(), symbol);
        if(done)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Company removed from watchlist", Map.of("symbol",symbol,"removed",true)));
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Company not found in watchlist"));
    }
}

