package majorproject.maf.controller;

import majorproject.maf.dto.request.AddBalance;
import majorproject.maf.dto.request.PreferenceRequest;
import majorproject.maf.dto.request.ProfileRequest;
import majorproject.maf.dto.response.Profile;
import majorproject.maf.dto.response.Share;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.dto.response.ApiResponse;
import majorproject.maf.service.ProfileService;
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
        UserDto u= (UserDto) authentication.getPrincipal();
        pS.createProfile(request,u.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successMessage("Profile created successfully"));
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<?>> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = pS.isUsernameAvailable(username);
        String message = isAvailable ? "Username is available" : "Username is already taken";
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(message, isAvailable));
    }

    @PostMapping("/create-preferences")
    public ResponseEntity<ApiResponse<?>> createPreferences(@RequestBody PreferenceRequest request, Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        pS.createPreferences(request,u.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successMessage("User preferences created successfully"));
    }

    @PutMapping("/update-preferences")
    public ResponseEntity<ApiResponse<?>> updatePreferences(@RequestBody PreferenceRequest request, Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        pS.updatePreferences(request,u.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successMessage("User preferences updated successfully"));
    }

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<?>> getPreferences(Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        PreferenceRequest preferences = pS.getPreferences(u);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User preferences fetched", preferences));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getProfile(Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        Profile user = pS.getProfile(u);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User profile fetched", user));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateUser(@RequestBody ProfileRequest request,Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        pS.updateProfile(request,u.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successMessage("User profile updated successfully"));
    }

    @PostMapping("/add-balance")
    public ResponseEntity<ApiResponse<?>> addBalance(@RequestBody AddBalance addBalance, Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        pS.addBalance(u.getId(), addBalance.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successMessage("Balance added successfully"));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<?>> getBalance(Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        Double balance = pS.getBalance(u.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User balance fetched", balance));
    }

    @GetMapping("/holdings")
    public ResponseEntity<ApiResponse<?>> getHoldings(Authentication authentication) {
        UserDto u= (UserDto) authentication.getPrincipal();
        List<Share> holdings = pS.getUserHoldings(u.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User Holdings fetched", holdings));
    }
}
