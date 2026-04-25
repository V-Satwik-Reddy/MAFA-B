package majorproject.maf.cache;

import majorproject.maf.dto.response.CompanyDto;
import majorproject.maf.dto.response.SectorDto;
import majorproject.maf.dto.response.StockChange;
import majorproject.maf.model.serving.CompanyMaster;
import majorproject.maf.repository.CompanyMasterRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PriceCacheService {

    CompanyMasterRepository companyMasterRepository;

    public PriceCacheService(CompanyMasterRepository companyMasterRepository) {
        this.companyMasterRepository = companyMasterRepository;
    }

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

    @Cacheable(value = "permanentCache", key = "'company::' + #symbol")
    public CompanyDto getBySymbol(String symbol) {
        CompanyMaster c=companyMasterRepository.findBySymbol(symbol);
        if(c==null){
            return null;
        }
        return new CompanyDto(c.getId(), c.getSymbol(), c.getName(), new SectorDto(c.getSector().getId(), c.getSector().getName()));
    }
}
