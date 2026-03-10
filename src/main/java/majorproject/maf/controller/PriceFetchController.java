package majorproject.maf.controller;

import majorproject.maf.dto.request.SymbolsRequest;
import majorproject.maf.dto.response.*;
import majorproject.maf.service.PriceFetch;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @PostMapping("/bulkstockprice")
    public ResponseEntity<ApiResponse<List<StockPriceDto>>> getBulkStockPrice(@RequestBody SymbolsRequest req){
        List<String> symbols = req.getSymbols();
        List<StockPriceDto> prices = priceFetch.fetchBulkCurrentPrice(symbols);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Fetched current prices for provided symbols", prices));
    }

    @GetMapping("/stockdailyprices")
    public ResponseEntity<ApiResponse<List<StockPriceDto>>> getAllPrices(
            @RequestParam String symbol,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beforeDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate afterDate) {
        HistoricalPricesWrapperDto allPrices = priceFetch.fetchLast100DailyPrice(symbol, limit, offset, startDate, endDate, beforeDate, afterDate);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Fetched last 30 days prices", allPrices.getHistoricalPrices()));
    }

    @GetMapping("/stockchange")
    public ResponseEntity<ApiResponse<StockChange>> getStockChange(@RequestParam String symbol) {
        StockChange stockChange = priceFetch.fetchPriceChange(symbol);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Fetched stock change", stockChange));
    }

    @GetMapping("/user-stockchange")
    public ResponseEntity<ApiResponse<List<StockChange>>> getUserStockChange(Authentication auth) {
        UserDto u = (UserDto) auth.getPrincipal();
        return ResponseEntity.ok().body(ApiResponse.success("Fetched user stock changes", priceFetch.fetchUserStockChanges(u)));
    }

    @PostMapping("/jobs/updateprices")
    public ResponseEntity<ApiResponse<Void>> updatePrices(){
        priceFetch.addPreviousDayPrices();
        return ResponseEntity.ok().body(ApiResponse.successMessage("Prices Updated Successfully"));
    }
}
