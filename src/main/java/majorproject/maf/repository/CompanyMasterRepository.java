package majorproject.maf.repository;

import majorproject.maf.model.serving.CompanyMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CompanyMasterRepository extends JpaRepository<CompanyMaster, Long> {

    @Query("""
    Select c from CompanyMaster c
    JOIN FETCH c.sector s
    WHERE c.symbol = :symbol
    """)
    CompanyMaster findBySymbol(String symbol);

    @Query("""     
    Select c from CompanyMaster c
    JOIN FETCH c.sector s
    """)
    List<CompanyMaster>  getAll();

    List<CompanyMaster> findByIdIn(Collection<Long> ids); // if frontend sends IDs

}
