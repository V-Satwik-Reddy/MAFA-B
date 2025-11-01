package majorproject.maf.controller;


import majorproject.maf.dto.UserDto;
import majorproject.maf.model.ApiResponse;
import majorproject.maf.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    ProfileService pS;

    public ProfileController(ProfileService pS) {
        this.pS = pS;
    }

    @GetMapping("/getuser/{email}")
    public ResponseEntity<ApiResponse<?>> getProfile(@PathVariable String email){

        UserDto user=pS.getProfile(email);
        return new ResponseEntity<>(new ApiResponse<>(true,"User found",user), HttpStatus.OK);
    }

    @GetMapping("/allusers")
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
//        UserPrinciple principal = (UserPrinciple) SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        System.out.println(principal.getUser());
        List<UserDto> users= pS.getUsers();
        return new ResponseEntity<>(new ApiResponse<>(true,"Users found",users), HttpStatus.OK);
    }

    @PutMapping("/user")
    public ResponseEntity<ApiResponse<?>> updateUser(@RequestBody UserDto user) {
        UserDto dto=pS.updateProfile(user);
        return new ResponseEntity<>(new ApiResponse<>(true,"User updated",dto), HttpStatus.OK);
    }
}
