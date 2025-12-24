package majorproject.maf.controller;

import majorproject.maf.dto.Share;
import majorproject.maf.dto.UserDto;
import majorproject.maf.model.ApiResponse;
import majorproject.maf.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserDto user = pS.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "User found", user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateUser(@RequestBody UserDto user) {
        UserDto dto=pS.updateProfile(user);
        return new ResponseEntity<>(new ApiResponse<>(true,"User updated",dto), HttpStatus.OK);
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<?>> getBalance(Authentication authentication) {
        double balance = pS.getBalance(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Balance fetched", balance));
    }

    @GetMapping("/holdings")
    public ResponseEntity<ApiResponse<?>> getHoldings(Authentication authentication) {
        List<Share> holdings = pS.getUserHoldings(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Holdings fetched", holdings));
    }
}
