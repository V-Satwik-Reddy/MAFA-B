package majorproject.maf.repository;

import jakarta.transaction.Transactional;
import majorproject.maf.model.user.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    @Query("""
    select wl from Watchlist wl
    join fetch wl.company c
    where wl.user.id = :userId
""")
    List<Watchlist> findByUser(int userId);

    @Query("""
    select wl from Watchlist wl
    join fetch wl.company c
    where wl.user.id = :userId and c.symbol = :symbol
""")
    Watchlist findBySymbolAndUserId(String symbol, int userId);

    @Query("""
    delete from Watchlist wl
    where wl.user.id = :userId and wl.company.symbol = :symbol
""")
    @Modifying
    @Transactional
    int deleteByUserIdAndCompanySymbol(int userId, String symbol);
}
