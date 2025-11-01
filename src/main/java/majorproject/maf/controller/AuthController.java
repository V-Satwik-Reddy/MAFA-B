package majorproject.maf.controller;

import jakarta.validation.Valid;
import majorproject.maf.dto.LoginRequest;
import majorproject.maf.dto.SignUpRequest;
import majorproject.maf.model.ApiResponse;
import majorproject.maf.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    AuthService auth;
    public AuthController(AuthService auth) {
        this.auth = auth;
    }
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signUp(@Valid @RequestBody SignUpRequest req) {
        System.out.println(req);
        ApiResponse<?> apiResponse = auth.signUp(req);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginRequest req) {

        return ResponseEntity.ok(auth.login(req));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify() {
        return ResponseEntity.ok("User is verified");
    }
}
