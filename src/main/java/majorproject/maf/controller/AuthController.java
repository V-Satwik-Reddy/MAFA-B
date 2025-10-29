package majorproject.maf.controller;

import jakarta.validation.Valid;
import majorproject.maf.dto.LoginRequest;
import majorproject.maf.dto.SignUpRequest;
import majorproject.maf.model.ApiResponse;
import majorproject.maf.dto.UserDto;
import majorproject.maf.model.UserPrinciple;
import majorproject.maf.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService auth;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUp(@Valid @RequestBody SignUpRequest req) {
        ApiResponse<?> apiResponse = auth.signUp(req);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest req) {
//        System.out.println(req);

        ApiResponse apiResponse=auth.login(req);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify() {
        return ResponseEntity.ok("User is verified");
    }
}
