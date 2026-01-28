package majorproject.maf.repository;

import majorproject.maf.model.serving.CompanyMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyMasterRepository extends JpaRepository<CompanyMaster, Long> {

    Optional<CompanyMaster> findBySymbol(String symbol);

    List<CompanyMaster> findByIdIn(Collection<Long> ids); // if frontend sends IDs
}
