package majorproject.maf.controller;


import majorproject.maf.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class Profile {

    @GetMapping("/getuser")
    public ApiResponse getProfile(@RequestParam String email){


        return new ApiResponse<>(true,"Profile ","Profile data does contain email or password");
    }
}
