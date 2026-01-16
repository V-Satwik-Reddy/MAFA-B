package majorproject.maf.repository;

import majorproject.maf.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, String> {

    public Stock findByUserIdAndSymbol(int user_id,String symbol);

    public List<Stock> findByUserId(int user_id);

//    @Modifying
//    @Query("""
//        UPDATE Stock h
//        SET h.shares = h.shares + :delta
//        WHERE h.user.id = :userId AND h.symbol = :symbol
//    """)
//    void incrementShares(int userId, String symbol, long delta);

//    @Modifying
//    @Query("""
//        UPDATE Stock h
//        SET h.shares = h.shares - :delta
//        WHERE h.user.id = :userId AND h.symbol = :symbol
//    """)
//    void decrementShares(int userId, String symbol, long delta);

    void deleteByUserIdAndSymbol(int id, String symbol);
}
