package majorproject.maf.service;

import majorproject.maf.dto.response.CompanyDto;
import majorproject.maf.dto.response.SectorDto;
import majorproject.maf.model.serving.CompanyMaster;
import majorproject.maf.model.serving.SectorMaster;
import majorproject.maf.repository.CompanyMasterRepository;
import majorproject.maf.repository.SectorMasterRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "permanentCache", key = "'allCompanies'")
    public List<CompanyDto> getAllCompanies() {
        return companyMasterRepository.findAll().stream().map(cp-> new CompanyDto(cp.getId(), cp.getSymbol(), cp.getName())).toList();
    }

    @Cacheable(value = "permanentCache", key = "'allSectors'")
    public List<SectorDto>  getAllSectors() {
        return sectorMasterRepository.findAll().stream().map(cp-> new SectorDto(cp.getId(), cp.getName())).toList();
    }

    @CacheEvict(value = "permanentCache", key = "'allSectors'")
    public String addSector(List<SectorDto> sector) {
        for (SectorDto sectorDto : sector) {
            SectorMaster sectorMaster = new SectorMaster();
            sectorMaster.setName(sectorDto.getName());
            sectorMasterRepository.save(sectorMaster);
        }
        return "success";
    }

    @CacheEvict(value = "permanentCache", key = "'allCompanies'")
    public String addCompany(List<CompanyDto> companies) {
        for (CompanyDto companyDto : companies) {
            CompanyMaster companyMaster = new CompanyMaster();
            companyMaster.setName(companyDto.getName());
            companyMaster.setSymbol(companyDto.getSymbol());
            companyMasterRepository.save(companyMaster);
        }
        return "success";
    }
}
