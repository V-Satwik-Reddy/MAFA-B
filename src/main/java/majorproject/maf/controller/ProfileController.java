package majorproject.maf.controller;

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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getProfile(Authentication authentication) {
        String email=authentication.getName();
        UserDto user = pS.getProfile(email);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User profile fetched", user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateUser(@RequestBody UserDto user) {
        UserDto dto=pS.updateProfile(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User profile updated successfully", dto));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<?>> getBalance(Authentication authentication) {
        double balance = pS.getBalance(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User Balance Fetched", balance));
    }

    @GetMapping("/holdings")
    public ResponseEntity<ApiResponse<?>> getHoldings(Authentication authentication) {
        List<Share> holdings = pS.getUserHoldings(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User Holdings fetched", holdings));
    }
}
