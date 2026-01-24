package majorproject.maf.controller;

import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
