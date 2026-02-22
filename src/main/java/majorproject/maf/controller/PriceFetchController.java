package majorproject.maf.controller;

import majorproject.maf.dto.request.SymbolsRequest;
import majorproject.maf.dto.response.*;
import majorproject.maf.model.StockPrice;
import majorproject.maf.service.PriceFetch;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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

    @PostMapping("/bulkstockprice")
    public ResponseEntity<ApiResponse<?>> getBulkStockPrice(@RequestBody SymbolsRequest req){
        Set<String> symbols = req.getSymbols();
        List<StockPriceDto> prices = priceFetch.fetchBulkCurrentPrice(symbols);
        ApiResponse<List<StockPriceDto>> response = new ApiResponse<>(true, "Fetched current prices for provided symbols", prices);
        return ResponseEntity.ok(response);
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
