package majorproject.maf.service;

import majorproject.maf.dto.response.CompanyDto;
import majorproject.maf.dto.response.SectorDto;
import majorproject.maf.model.CompanyMaster;
import majorproject.maf.model.SectorMaster;
import majorproject.maf.repository.CompanyMasterRepository;
import majorproject.maf.repository.SectorMasterRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MasterDataService {

    private final CompanyMasterRepository companyMasterRepository;
    private final SectorMasterRepository sectorMasterRepository;
    public MasterDataService(CompanyMasterRepository companyMasterRepository, SectorMasterRepository sectorMasterRepository) {
        this.sectorMasterRepository = sectorMasterRepository;
        this.companyMasterRepository = companyMasterRepository;
    }

    @Cacheable(value = "permanentCache", key = "'allCompanies'")
    public List<CompanyDto> getAllCompanies() {
        return companyMasterRepository.findAll().stream().map(cp-> new CompanyDto(cp.getId(), cp.getSymbol(), cp.getName())).toList();
    }

    @Cacheable(value = "permanentCache", key = "'allSectors'")
    public List<SectorDto>  getAllSectors() {
        return sectorMasterRepository.findAll().stream().map(cp-> new SectorDto(cp.getId(), cp.getName())).toList();
    }

}
