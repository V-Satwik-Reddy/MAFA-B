package majorproject.maf.repository;

import majorproject.maf.model.user.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
