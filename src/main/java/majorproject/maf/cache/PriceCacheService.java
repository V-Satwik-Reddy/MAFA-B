package majorproject.maf.cache;

import majorproject.maf.dto.response.StockChange;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PriceCacheService {

    @Cacheable(value = "priceChanges",key = "#symbol", unless = "#result == null")
    public StockChange cacheStockChange(String symbol,StockChange stockChange) {
        return stockChange;
    }

    @CacheEvict(value="currentprices", allEntries = true)
    public void evictCurrentPriceCache() {
        // This method will remove all entries from the "currentprices" cache
    }

    @CacheEvict(value="historicalPrices", allEntries = true)
    public void evictHistoricalPriceCache() {
        // This method will remove all entries from the "currentprices" cache
    }
}
