package majorproject.maf.repository;

import majorproject.maf.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Stock findByUserIdAndSymbol(int user_id,String symbol);

    List<Stock> findByUserId(int user_id);

    @Modifying
    @Query("""
        UPDATE Stock h
        SET h.shares = h.shares + :delta
        WHERE h.user.id = :userId AND h.symbol = :symbol
    """)
    void incrementShares(int userId, String symbol, long delta);

    @Modifying
    @Query("""
        UPDATE Stock h
        SET h.shares = h.shares - :quantity
        WHERE h.user.id = :userId AND h.symbol = :symbol AND h.shares >= :quantity
    """)
    int decrementIfSufficientShares(int id, String symbol, long quantity);

    @Modifying
    @Query("""
    Delete from Stock s
    where s.user.id=:userId and s.symbol=:symbol and s.shares=0
""")
    void deleteIfSharesZero(int id, String symbol);
}
