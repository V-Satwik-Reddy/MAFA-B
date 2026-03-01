package majorproject.maf.repository;

import majorproject.maf.model.serving.SectorMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface SectorMasterRepository extends JpaRepository<SectorMaster, Long> {

    Optional<SectorMaster> findByName(String name);

    List<SectorMaster> findByIdIn(Collection<Long> ids);

    @Query("""
    select sm.name,sm from SectorMaster sm
    where sm.name in :names
""")
    Map<String,SectorMaster> findByNameIn(Collection<String> names);
}

