package majorproject.maf.service;

import majorproject.maf.model.CompanyMaster;
import majorproject.maf.model.SectorMaster;
import majorproject.maf.repository.CompanyMasterRepository;
import majorproject.maf.repository.SectorMasterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterDataService {

    private final CompanyMasterRepository companyMasterRepository;
    private final SectorMasterRepository sectorMasterRepository;
    public MasterDataService(CompanyMasterRepository companyMasterRepository, SectorMasterRepository sectorMasterRepository) {
        this.sectorMasterRepository = sectorMasterRepository;
        this.companyMasterRepository = companyMasterRepository;
    }

    public List<CompanyMaster> getAllCompanies() {
        return companyMasterRepository.findAll();
    }

    public List<SectorMaster>  getAllSectors() {
        return sectorMasterRepository.findAll();
    }
}
