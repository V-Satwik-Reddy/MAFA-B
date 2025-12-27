package majorproject.maf.repository;

import majorproject.maf.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    List<StockPrice> findBySymbolOrderByDateDesc(String symbol);

    StockPrice findTopBySymbolOrderByDateDesc(String symbol);

    @Query("select symbol from StockPrice group by symbol")
    List<String> findAllSymbols();
}
