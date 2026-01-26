package majorproject.maf.repository;

import majorproject.maf.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    List<StockPrice> findBySymbolOrderByDateDesc(String symbol);

    StockPrice findTopBySymbolOrderByDateDesc(String symbol);

    @Query("select symbol from StockPrice group by symbol")
    List<String> findAllSymbols();

    @Query("""
    select sp.close
    from StockPrice sp
    where sp.symbol in :symbols
    and sp.date = (
        select max(sp2.date) from StockPrice sp2
    ) order by sp.symbol
    """)
    List<Double> batchFind(List<String> symbols);

    @Query("""
        select sp.close
        from StockPrice sp
        where sp.symbol = :symbol
        order by sp.date desc
        limit 2
    """)
    List<Double> singleSymbolPriceChange(String symbol);

    @Query("""
    select sp
    from StockPrice sp
    where sp.symbol in :symbols
    order by sp.date desc
    limit :length
""")
    List<StockPrice> multipleSymbolPriceChange(Set<String> symbols, int length);
}
