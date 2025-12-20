package majorproject.maf.controller;


import majorproject.maf.dto.UserDto;
import majorproject.maf.model.ApiResponse;
import majorproject.maf.service.DashboardService;
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
    DashboardService ds;
    public ProfileController(ProfileService pS, DashboardService ds) {
        this.ds = ds;
        this.pS = pS;
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getProfile(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserDto user = pS.getProfile(userDetails.getUsername()); // username/email
//        ds.getHoldingsDetails(user.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, "User found", user));
    }

    @PutMapping("/user")
    public ResponseEntity<ApiResponse<?>> updateUser(@RequestBody UserDto user) {
        UserDto dto=pS.updateProfile(user);
        return new ResponseEntity<>(new ApiResponse<>(true,"User updated",dto), HttpStatus.OK);
    }
}
