package majorproject.maf.controller;

import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.dto.response.CompanyDto;
import majorproject.maf.dto.response.SectorDto;
import majorproject.maf.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<?>> getCompanies() {
        return ResponseEntity.ok(ApiResponse.success("Companies fetched successfully", masterDataService.getAllCompanies()));
    }

    @GetMapping("/sectors")
    public ResponseEntity<ApiResponse<?>> getSectors() {
        return ResponseEntity.ok(ApiResponse.success("Sectors fetched successfully", masterDataService.getAllSectors()));
    }

    @PostMapping("/sectors")
    public ResponseEntity<ApiResponse<?>> addSector(@RequestBody List<SectorDto> sector) {
        return ResponseEntity.ok(ApiResponse.success("Sector added successfully", masterDataService.addSector(sector)));
    }

    @PostMapping("/companies")
    public ResponseEntity<ApiResponse<?>> addCompany(@RequestBody List<CompanyDto> companies) {
        return ResponseEntity.ok(ApiResponse.success("Company added successfully", masterDataService.addCompany(companies)));
    }
}
