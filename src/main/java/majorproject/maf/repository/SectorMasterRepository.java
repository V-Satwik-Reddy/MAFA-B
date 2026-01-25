package majorproject.maf.repository;

import majorproject.maf.model.SectorMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SectorMasterRepository extends JpaRepository<SectorMaster, Long> {

    Optional<SectorMaster> findByName(String name);

    List<SectorMaster> findByIdIn(Collection<Long> ids);
}

