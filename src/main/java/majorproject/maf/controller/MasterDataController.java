package majorproject.maf.controller;

import majorproject.maf.dto.request.SymbolsRequest;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.dto.response.CompanyDto;
import majorproject.maf.dto.response.SectorDto;
import majorproject.maf.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<CompanyDto>>> getCompanies() {
        List<CompanyDto> companies = masterDataService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.success("Companies fetched successfully", companies));
    }

    @GetMapping("/companies/{symbol}")
    public ResponseEntity<ApiResponse<CompanyDto>> getCompanyBySymbol(@PathVariable String symbol) {
        CompanyDto company = masterDataService.getBySymbol(symbol);
        return ResponseEntity.ok(ApiResponse.success("Companies fetched successfully", company));
    }

    @PostMapping("/companies/by-symbols")
    public ResponseEntity<ApiResponse<List<CompanyDto>>> getCompaniesBySymbols(
            @RequestBody SymbolsRequest request) {

        List<CompanyDto> companies = request.getSymbols().stream()
                .map(masterDataService::getBySymbol)
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Companies fetched successfully", companies)
        );
    }

    @GetMapping("/sectors")
    public ResponseEntity<ApiResponse<List<SectorDto>>> getSectors() {
        List<SectorDto> sectors = masterDataService.getAllSectors();
        return ResponseEntity.ok(ApiResponse.success("Sectors fetched successfully", sectors));
    }

    @PostMapping("/sectors")
    public ResponseEntity<ApiResponse<Void>> addSector(@RequestBody List<SectorDto> sector) {
        masterDataService.addSector(sector);
        return ResponseEntity.ok(ApiResponse.successMessage("Sector added successfully"));
    }

    @PostMapping("/companies")
    public ResponseEntity<ApiResponse<Void>> addCompany(@RequestBody List<CompanyDto> companies) {
        masterDataService.addCompany(companies);
        return ResponseEntity.ok(ApiResponse.successMessage("Company added successfully"));
    }
}
