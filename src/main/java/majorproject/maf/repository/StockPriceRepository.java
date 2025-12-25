package majorproject.maf.repository;

import majorproject.maf.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    List<StockPrice> findBySymbolOrderByDateDesc(String symbol);

    StockPrice findTopBySymbolOrderByDateDesc(String symbol);
}
