package majorproject.maf.controller;

import jakarta.validation.Valid;
import majorproject.maf.dto.LoginRequest;
import majorproject.maf.dto.SignUpRequest;
import majorproject.maf.model.ApiResponse;
import majorproject.maf.dto.UserDto;
import majorproject.maf.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import majorproject.maf.model.User;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService auth;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUp(@Valid @RequestBody SignUpRequest req) {
        UserDto dto = auth.signUp(req);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sign Up Successful", dto));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest req) {
        UserDto dto=auth.login(req);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login Successful", dto));
    }
}
