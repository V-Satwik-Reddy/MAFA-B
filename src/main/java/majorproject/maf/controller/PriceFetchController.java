package majorproject.maf.controller;

import majorproject.maf.service.PriceFetch;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
