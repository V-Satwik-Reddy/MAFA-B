package majorproject.maf.repository;

import majorproject.maf.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    int decrementIfSufficientShares(
            @Param("userId") int userId,
            @Param("symbol") String symbol,
            @Param("quantity") long quantity
    );

    @Modifying
    @Query("""
    DELETE FROM Stock h
    WHERE h.user.id = :userId AND h.symbol = :symbol AND h.shares <= 0
""")
    void deleteIfSharesZero(
            @Param("userId") int userId,
            @Param("symbol") String symbol
    );
}
