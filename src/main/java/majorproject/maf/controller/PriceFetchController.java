package majorproject.maf.controller;

import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.dto.response.StockChange;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.model.StockPrice;
import majorproject.maf.service.PriceFetch;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PriceFetchController {

    PriceFetch priceFetch;
    public PriceFetchController(PriceFetch priceFetch) {
        this.priceFetch = priceFetch;
    }

    @GetMapping("/stockprice")
    public ResponseEntity<Double> getStockPrice(@RequestParam String symbol) {
        double stockPrice = priceFetch.fetchCurrentPrice(symbol);
        return ResponseEntity.ok(stockPrice);
    }

    @GetMapping("/stockdailyprices")
    public ResponseEntity<ApiResponse<?>> getAllPrices(@RequestParam String symbol) {
        List<StockPrice> allPrices = priceFetch.fetchLast100DailyPrice(symbol);
        ApiResponse<List<StockPrice>> response = new ApiResponse<>(true, "Fetched last 30 days prices", allPrices);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stockchange")
    public ResponseEntity<?> getStockChange(@RequestParam String symbol) {
        StockChange stockChange = priceFetch.fetchPriceChange(symbol);
        return ResponseEntity.ok(stockChange);
    }

    @GetMapping("/user-stockchange")
    public ResponseEntity<?> getUserStockChange(Authentication auth) {
        UserDto u = (UserDto) auth.getPrincipal();
        return ResponseEntity.ok().body(ApiResponse.success("Fetched user stock changes", priceFetch.fetchUserStockChanges(u)));
    }

    @PostMapping("/jobs/updateprices")
    public ResponseEntity<?> updatePrices(){
        priceFetch.addPreviousDayPrices();
        return ResponseEntity.ok("Prices Updated Successfully");
    }
}
