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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MasterDataService {

    private final CompanyMasterRepository companyMasterRepository;
    private final SectorMasterRepository sectorMasterRepository;
    private final PriceFetch priceFetch;

    public MasterDataService(CompanyMasterRepository companyMasterRepository, SectorMasterRepository sectorMasterRepository, PriceFetch priceFetch) {
        this.sectorMasterRepository = sectorMasterRepository;
        this.companyMasterRepository = companyMasterRepository;
        this.priceFetch = priceFetch;
    }

    @Cacheable(value = "permanentCache", key = "'allCompanies'")
    public List<CompanyDto> getAllCompanies() {
        return companyMasterRepository.getAll().stream().map(cp-> new CompanyDto(cp.getId(), cp.getSymbol(), cp.getName(),new SectorDto(cp.getSector().getId(),cp.getSector().getName()))).toList();
    }

    @Cacheable(value = "permanentCache", key = "'allSectors'")
    public List<SectorDto>  getAllSectors() {
        return sectorMasterRepository.findAll().stream().map(cp-> new SectorDto(cp.getId(), cp.getName())).toList();
    }

    @CacheEvict(value = "permanentCache", key = "'allSectors'")
    public void addSector(List<SectorDto> sectors) {
        List<SectorMaster> entities = sectors.stream()
                .map(s -> new SectorMaster(s.getName()))
                .toList();
        sectorMasterRepository.saveAll(entities);
    }

    @CacheEvict(value = "permanentCache", key = "'allCompanies'")
    public void addCompany(List<CompanyDto> companies) {
        Set<String> sectorNames = companies.stream().map(CompanyDto::getSector).map(SectorDto::getName).collect(Collectors.toSet());
        List<SectorMaster> allSectorMap = sectorMasterRepository.findByNameIn(sectorNames);
        Map<String, SectorMaster> sectorMap = allSectorMap.stream().collect(Collectors.toMap(SectorMaster::getName, s -> s));
        List<CompanyMaster> companyMasters= new ArrayList<>();
        for (CompanyDto companyDto : companies) {
            CompanyMaster companyMaster = new CompanyMaster();
            companyMaster.setName(companyDto.getName());
            companyMaster.setSymbol(companyDto.getSymbol());
            companyMaster.setSector(sectorMap.get(companyDto.getSector().getName()));
            priceFetch.fetchCurrentPrice(companyDto.getSymbol());
            companyMasters.add(companyMaster);
        }
        companyMasterRepository.saveAll(companyMasters);
    }

    @Cacheable(value = "permanentCache", key = "'company::' + #symbol")
    public CompanyDto getBySymbol(String symbol) {
        CompanyMaster c=companyMasterRepository.findBySymbol(symbol);
        return new CompanyDto(c.getId(), c.getSymbol(), c.getName(), new SectorDto(c.getSector().getId(), c.getSector().getName()));
    }
}
