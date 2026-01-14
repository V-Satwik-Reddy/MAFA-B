package majorproject.maf.controller;

import majorproject.maf.dto.request.ProfileRequest;
import majorproject.maf.dto.response.Share;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.service.ProfileService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/profile")
public class ProfileController {

    ProfileService pS;
    public ProfileController(ProfileService pS) {
        this.pS = pS;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createProfile(@RequestBody ProfileRequest request,Authentication authentication) {
        pS.createProfile(request,(int) authentication.getDetails());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successMessage("Profile created successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getProfile(Authentication authentication) {
        ProfileRequest user = pS.getProfile((int) authentication.getDetails());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User profile fetched", user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateUser(@RequestBody ProfileRequest request,Authentication authentication) {
        pS.createProfile(request,(int) authentication.getDetails());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successMessage("User profile updated successfully"));
    }

    @GetMapping("/holdings")
    public ResponseEntity<ApiResponse<?>> getHoldings(Authentication authentication) {
        List<Share> holdings = pS.getUserHoldings(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User Holdings fetched", holdings));
    }
}
