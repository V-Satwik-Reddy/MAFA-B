package majorproject.maf.cache;

import majorproject.maf.dto.response.StockChange;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PriceCacheService {

    @Cacheable(value = "priceChanges",key = "#symbol", unless = "#result == null")
    public StockChange cacheStockChange(String symbol,StockChange stockChange) {
        return stockChange;
    }

}
